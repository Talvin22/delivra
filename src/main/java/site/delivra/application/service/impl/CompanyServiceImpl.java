package site.delivra.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.delivra.application.exception.DataExistException;
import site.delivra.application.exception.InvalidDataException;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.mapper.UserMapper;
import site.delivra.application.model.dto.company.CompanyDTO;
import site.delivra.application.model.dto.company.CompanyStatsDTO;
import site.delivra.application.model.dto.company.GlobalStatsDTO;
import site.delivra.application.model.dto.user.UserProfileDTO;
import site.delivra.application.model.entities.Company;
import site.delivra.application.model.entities.RefreshToken;
import site.delivra.application.model.entities.Role;
import site.delivra.application.model.entities.User;
import site.delivra.application.model.enums.CompanyStatus;
import site.delivra.application.model.enums.DeliveryTaskStatus;
import site.delivra.application.model.enums.RegistrationStatus;
import site.delivra.application.model.request.company.CompanyRegistrationRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.repository.*;
import site.delivra.application.security.JwtTokenProvider;
import site.delivra.application.service.CompanyService;
import site.delivra.application.service.RefreshTokenService;
import site.delivra.application.service.model.DelivraServiceUserRole;
import site.delivra.application.utils.ApiUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DeliveryTaskRepository taskRepository;
    private final NavigationSessionRepository navSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final ApiUtils apiUtils;

    @Override
    @Transactional
    public DelivraResponse<UserProfileDTO> registerCompany(CompanyRegistrationRequest request) {
        if (companyRepository.existsByName(request.getCompanyName())) {
            throw new DataExistException("Company name already taken: " + request.getCompanyName());
        }
        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new DataExistException("Email already exists: " + request.getAdminEmail());
        }
        if (userRepository.existsByUsername(request.getAdminUsername())) {
            throw new DataExistException("Username already exists: " + request.getAdminUsername());
        }
        if (!request.getAdminPassword().equals(request.getConfirmPassword())) {
            throw new InvalidDataException("Passwords do not match");
        }

        Company company = new Company();
        company.setName(request.getCompanyName());
        company.setEmail(request.getAdminEmail());
        company.setStatus(CompanyStatus.TRIAL);
        company.setTrialEndsAt(LocalDateTime.now().plusDays(14));
        Company savedCompany = companyRepository.save(company);

        Role adminRole = roleRepository.findByName(DelivraServiceUserRole.ADMIN.getRole())
                .orElseThrow(() -> new NotFoundException("Admin role not found"));

        User admin = new User();
        admin.setUsername(request.getAdminUsername());
        admin.setEmail(request.getAdminEmail());
        admin.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        admin.setStatus(RegistrationStatus.ACTIVE);
        admin.setCompany(savedCompany);
        admin.setRoles(Set.of(adminRole));
        User savedAdmin = userRepository.save(admin);

        RefreshToken refreshToken = refreshTokenService.generateOrUpdateRefreshToken(savedAdmin);
        String token = jwtTokenProvider.generateToken(savedAdmin);
        UserProfileDTO dto = userMapper.toUserProfileDto(savedAdmin, token, refreshToken.getToken());
        dto.setToken(token);
        return DelivraResponse.createSuccessfulWithNewToken(dto);
    }

    @Override
    public DelivraResponse<CompanyDTO> getMyCompany() {
        Integer companyId = apiUtils.getCompanyIdFromAuthentication();
        Company company = companyRepository.findByIdAndDeletedFalse(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));
        return DelivraResponse.createSuccessful(toDto(company));
    }

    @Override
    public DelivraResponse<CompanyStatsDTO> getMyStats() {
        Integer companyId = apiUtils.getCompanyIdFromAuthentication();
        Company company = companyRepository.findByIdAndDeletedFalse(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));
        return DelivraResponse.createSuccessful(buildStats(company));
    }

    @Override
    public DelivraResponse<ArrayList<CompanyDTO>> getAllCompanies() {
        ArrayList<CompanyDTO> companies = companyRepository.findAllByDeletedFalse()
                .stream().map(this::toDto).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return DelivraResponse.createSuccessful(companies);
    }

    @Override
    public DelivraResponse<CompanyStatsDTO> getCompanyStats(Integer companyId) {
        Company company = companyRepository.findByIdAndDeletedFalse(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found: " + companyId));
        return DelivraResponse.createSuccessful(buildStats(company));
    }

    @Override
    public DelivraResponse<GlobalStatsDTO> getGlobalStats() {
        GlobalStatsDTO stats = GlobalStatsDTO.builder()
                .totalCompanies(companyRepository.countByDeletedFalse())
                .activeCompanies(companyRepository.countByDeletedFalseAndStatus(CompanyStatus.ACTIVE))
                .trialCompanies(companyRepository.countByDeletedFalseAndStatus(CompanyStatus.TRIAL))
                .suspendedCompanies(companyRepository.countByDeletedFalseAndStatus(CompanyStatus.SUSPENDED))
                .totalTasks(taskRepository.countByDeletedFalse())
                .completedTasks(taskRepository.countByDeletedFalseAndStatus(DeliveryTaskStatus.COMPLETED))
                .inProgressTasks(taskRepository.countByDeletedFalseAndStatus(DeliveryTaskStatus.IN_PROGRESS))
                .totalUsers(userRepository.countByDeletedFalse())
                .totalNavigations(navSessionRepository.count())
                .build();
        return DelivraResponse.createSuccessful(stats);
    }

    @Override
    public DelivraResponse<CompanyDTO> updateCompanyStatus(Integer companyId, CompanyStatus status) {
        Company company = companyRepository.findByIdAndDeletedFalse(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found: " + companyId));
        company.setStatus(status);
        company.setUpdated(LocalDateTime.now());
        companyRepository.save(company);
        return DelivraResponse.createSuccessful(toDto(company));
    }

    private CompanyStatsDTO buildStats(Company company) {
        Integer id = company.getId();
        return CompanyStatsDTO.builder()
                .companyId(id)
                .companyName(company.getName())
                .status(company.getStatus())
                .trialEndsAt(company.getTrialEndsAt())
                .totalTasks(taskRepository.countByDeletedFalseAndCompany_Id(id))
                .completedTasks(taskRepository.countByDeletedFalseAndCompany_IdAndStatus(id, DeliveryTaskStatus.COMPLETED))
                .inProgressTasks(taskRepository.countByDeletedFalseAndCompany_IdAndStatus(id, DeliveryTaskStatus.IN_PROGRESS))
                .pendingTasks(taskRepository.countByDeletedFalseAndCompany_IdAndStatus(id, DeliveryTaskStatus.PENDING))
                .canceledTasks(taskRepository.countByDeletedFalseAndCompany_IdAndStatus(id, DeliveryTaskStatus.CANCELED))
                .totalDrivers(userRepository.countByRoleAndCompany("DRIVER", id))
                .totalDispatchers(userRepository.countByRoleAndCompany("DISPATCHER", id))
                .totalNavigations(navSessionRepository.countByCompanyId(id))
                .build();
    }

    private CompanyDTO toDto(Company c) {
        return CompanyDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .status(c.getStatus())
                .trialEndsAt(c.getTrialEndsAt())
                .created(c.getCreated())
                .build();
    }
}
