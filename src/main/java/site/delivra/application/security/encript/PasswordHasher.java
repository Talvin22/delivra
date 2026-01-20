package site.delivra.application.security.encript;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    public static void main(String[] args) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        String firstPassword = bCryptPasswordEncoder.encode("password11111!");
        String secondPassword = bCryptPasswordEncoder.encode("password22222!");
        String thirdPassword = bCryptPasswordEncoder.encode("password33333!");

        System.out.println("First password " + firstPassword);
        System.out.println("secondPassword " + secondPassword);
        System.out.println("thirdPassword " + thirdPassword);

    }
}
