package com.example.pdp.controllers;

import com.example.pdp.models.auth.JwtResponse;
import com.example.pdp.models.auth.ResponseMessage;
import com.example.pdp.models.auth.SignUpForm;
import com.example.pdp.models.User;
import com.example.pdp.models.Role;
import com.example.pdp.models.RoleName;
import com.example.pdp.models.auth.LoginForm;
import com.example.pdp.repositories.RoleRepository;
import com.example.pdp.repositories.UserRepository;
import com.example.pdp.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping("/auth")
public class AuthRESTController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;


    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtProvider jwtProvider;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateJwtToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt,userDetails.getUsername(), userDetails.getAuthorities()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signUpRequest) {
        System.out.println(signUpRequest.getRole());
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity<>(new ResponseMessage("Fail -> Username is already taken."), HttpStatus.BAD_REQUEST);
        }


        // Create user account
        User user = new User(signUpRequest.getUsername(), passwordEncoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        strRoles.forEach(role -> {
            switch (role.toUpperCase()) {
                case "ADMIN":
                    Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Fail -> Cause: Admin Role not found."));
                    roles.add(adminRole);
                    break;
                case "MATH":
                    Role mathRole = roleRepository.findByName(RoleName.ROLE_MATH)
                            .orElseThrow(() -> new RuntimeException("Fail -> Cause: Math Role not found."));
                    roles.add(mathRole);
                    break;
                case "PHYS":
                    Role physRole = roleRepository.findByName(RoleName.ROLE_PHYS)
                            .orElseThrow(() -> new RuntimeException("Fail -> Cause: Phys Role not found."));
                    roles.add(physRole);
                    break;
                case "CHEM":
                    Role chemRole = roleRepository.findByName(RoleName.ROLE_CHEM)
                            .orElseThrow(() -> new RuntimeException("Fail -> Cause: Chem Role not found."));
                    roles.add(chemRole);
                    break;
            }
        });

        user.setRoles(roles);
        userRepository.save(user);

        return new ResponseEntity<>(new ResponseMessage("User registered successfully."), HttpStatus.OK);

    }

}
