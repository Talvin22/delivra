package site.delivra.application.service;

import site.delivra.application.model.dto.company.CompanyDTO;
import site.delivra.application.model.dto.company.CompanyStatsDTO;
import site.delivra.application.model.dto.company.GlobalStatsDTO;
import site.delivra.application.model.dto.user.UserProfileDTO;
import site.delivra.application.model.enums.CompanyStatus;
import site.delivra.application.model.request.company.CompanyRegistrationRequest;
import site.delivra.application.model.response.DelivraResponse;

import java.util.ArrayList;

public interface CompanyService {

    DelivraResponse<UserProfileDTO> registerCompany(CompanyRegistrationRequest request);

    DelivraResponse<CompanyDTO> getMyCompany();

    DelivraResponse<CompanyStatsDTO> getMyStats();

    DelivraResponse<ArrayList<CompanyDTO>> getAllCompanies();

    DelivraResponse<CompanyStatsDTO> getCompanyStats(Integer companyId);

    DelivraResponse<GlobalStatsDTO> getGlobalStats();

    DelivraResponse<CompanyDTO> updateCompanyStatus(Integer companyId, CompanyStatus status);
}
