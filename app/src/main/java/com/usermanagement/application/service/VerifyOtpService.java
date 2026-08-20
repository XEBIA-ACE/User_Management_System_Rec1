package com.usermanagement.application.service;

import com.usermanagement.application.port.in.VerifyOtpUseCase;
import com.usermanagement.application.port.in.command.VerifyOtpCommand;
import com.usermanagement.application.port.out.OtpRepository;
import com.usermanagement.application.port.out.UserRepository;
import com.usermanagement.domain.exception.InvalidOtpException;
import com.usermanagement.domain.exception.UserNotFoundException;
import com.usermanagement.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyOtpService implements VerifyOtpUseCase {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;

    @Override
    @Transactional
    public void verify(VerifyOtpCommand command) {
        String storedOtp = otpRepository.findOtp(command.email())
                .orElseThrow(InvalidOtpException::new);

        if (!storedOtp.equals(command.otp())) {
            throw new InvalidOtpException();
        }

        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UserNotFoundException(command.email()));

        user.activate();
        userRepository.save(user);
        otpRepository.deleteOtp(command.email());

        log.info("User verified: {}", user.getId());
    }
}
