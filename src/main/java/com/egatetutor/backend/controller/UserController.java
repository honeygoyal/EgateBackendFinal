package com.egatetutor.backend.controller;

import javax.imageio.ImageIO;
import javax.validation.Valid;
import javax.validation.constraints.Null;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.egatetutor.backend.model.UserInfo;
import com.egatetutor.backend.model.responsemodel.JwtRequest;
import com.egatetutor.backend.model.responsemodel.JwtResponse;
import com.egatetutor.backend.model.responsemodel.UserResponse;
import com.egatetutor.backend.repository.UserRepository;
import com.egatetutor.backend.service.UserService;
import com.egatetutor.backend.util.JwtTokenUtil;
import io.swagger.annotations.ApiOperation;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;


@CrossOrigin(origins = "https://trusting-euclid-a0c1c2.netlify.app")
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private AmazonS3 s3client;

    @Value("${jsa.s3.bucket}")
    private String bucketName;

    @Autowired
    private Environment env;

    @PostMapping("/register")
    public ResponseEntity createUser(@Valid @RequestBody UserInfo userdetails)
            throws Exception {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserInfo userDto = modelMapper.map(userdetails, UserInfo.class);
        UserInfo createdUser = null;
        try {
            createdUser = userService.createUser(userDto);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Exception("Email ID already Registered"));
        }
        UserResponse userResponse = modelMapper.map(createdUser, UserResponse.class);
        return ResponseEntity.status(HttpStatus.OK).body(userResponse);
    }


    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userService.loadUserByUsername(authenticationRequest.getUsername());
        UserInfo userEntity = userRepository.findByEmailId(userDetails.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);
        System.out.println("Token: "+ token);
        System.out.println("User Entity: "+ userEntity);
        if (userEntity != null && userEntity.getPhoto() != null && !userEntity.getPhoto().isEmpty()) {
            try {
                S3Object profileObj = s3client.getObject(new GetObjectRequest(bucketName, userEntity.getPhoto()));
                byte[] profileImage = IOUtils.toByteArray(profileObj.getObjectContent()); //Files.readAllBytes(profileImagepath);
                String encodedProfilePhoto = Base64.getEncoder().encodeToString(profileImage);
                userEntity.setPhoto(encodedProfilePhoto);
            } catch (Exception exception) {
                System.out.println("Path is not correct");
            }
        }
            JwtResponse jwtResponse = new JwtResponse(token, userEntity);
            return ResponseEntity.status(HttpStatus.OK).body(jwtResponse);
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    @PostMapping("/uploadProfileData")
    @ApiOperation(value = "Make a POST request to upload the file",
            produces = "text/plain", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequestMapping(value = "uploadProfileData", method = RequestMethod.POST)
    public ResponseEntity<UserInfo> uploadProfileData(
            @RequestPart(value = "profileFile", required = true) MultipartFile profileFile,
            @RequestPart(value = "signatureFile", required = true) MultipartFile signatureFile,
            @RequestPart(value = "govtIdFile", required = true) MultipartFile govtIdFile,
            Long userId
    ) throws IOException {
        String BASE_URL = env.getProperty("profile_base_url");
        StringBuilder profileUrl = new StringBuilder();
        StringBuilder signatureUrl = new StringBuilder();
        StringBuilder govtIdUrl = new StringBuilder();
        String photo = "", signature = "", govtId = "";
        profileUrl.append(BASE_URL);
        profileUrl.append("profile_");
        profileUrl.append(userId);
        signatureUrl.append(BASE_URL);
        signatureUrl.append("signature_");
        signatureUrl.append(userId);
        govtIdUrl.append(BASE_URL);
        govtIdUrl.append("govtId_");
        govtIdUrl.append(userId);
        List<MultipartFile> files = new ArrayList<>();
        files.add(profileFile);
        files.add(signatureFile);
        files.add(govtIdFile);
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            byte[] pictureData = file.getBytes();
            InputStream stream = new ByteArrayInputStream(pictureData);
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(pictureData.length);
            meta.setContentType("image/png");
            switch (i) {
                case 0:
                    s3client.putObject(new PutObjectRequest(
                            bucketName, profileUrl.toString(), stream, meta)
                            .withCannedAcl(CannedAccessControlList.Private));
                    stream.close();
                    photo = Base64.getEncoder().encodeToString(pictureData);
                    break;
                case 1:
                    s3client.putObject(new PutObjectRequest(
                            bucketName, signatureUrl.toString(), stream, meta)
                            .withCannedAcl(CannedAccessControlList.Private));
                    stream.close();
                    signature = Base64.getEncoder().encodeToString(pictureData);
                    break;
                case 2:
                    s3client.putObject(new PutObjectRequest(
                            bucketName, govtIdUrl.toString(), stream, meta)
                            .withCannedAcl(CannedAccessControlList.Private));
                    stream.close();
                    govtId = Base64.getEncoder().encodeToString(pictureData);
                    break;
            }
        }
        Optional<UserInfo> userInfo = userRepository.findById(userId);
        if (userInfo.isPresent()) {
            UserInfo user = userInfo.get();
            user.setPhoto(profileUrl.toString());
            user.setSignature(signatureUrl.toString());
            user.setGovtId(govtIdUrl.toString());
            userRepository.save(user);
            user.setPhoto(photo);
            user.setGovtId(govtId);
            user.setSignature(signature);
            return ResponseEntity.status(HttpStatus.OK).body(user);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }


    @GetMapping("/findAllUnVerifiedUser")
    public List<UserInfo> findAllUnVerifiedUser(long userId) throws Exception {
        Optional<UserInfo> userInfo = userRepository.findById(userId);
        if (userInfo.isPresent() && userInfo.get().getIsAdmin()) {
            List<UserInfo> userInfoList = userRepository.findAllUnVerifiedUser();
            for (UserInfo user : userInfoList) {

                String profileString = "", signatureString = "", govtIdString = "";
                try {
					S3Object profileObj = s3client.getObject(new GetObjectRequest(bucketName, user.getPhoto()));
					byte[] profileImage = IOUtils.toByteArray(profileObj.getObjectContent());
					S3Object signatureObj = s3client.getObject(new GetObjectRequest(bucketName, user.getSignature()));
					byte[] signatureImage = IOUtils.toByteArray(signatureObj.getObjectContent());
					S3Object govtObj = s3client.getObject(new GetObjectRequest(bucketName, user.getGovtId()));
					byte[] govtIdImage = IOUtils.toByteArray(govtObj.getObjectContent());
                    profileString = Base64.getEncoder().encodeToString(profileImage);
                    signatureString = Base64.getEncoder().encodeToString(signatureImage);
                    govtIdString = Base64.getEncoder().encodeToString(govtIdImage);
                } catch (Exception ex) {
                    System.out.println("File not present");
                }
                user.setPhoto(profileString);
                user.setSignature(signatureString);
                user.setGovtId(govtIdString);
            }
            return userInfoList;
        }
        return null;
    }

    @PostMapping("/verification")
    public ResponseEntity<String> verification(long userId, boolean isVerified, String comment) throws Exception {
        Optional<UserInfo> userInfo = userRepository.findById(userId);
        if (!userInfo.isPresent()) throw new Exception("User doesn't Exists");
        UserInfo user = userInfo.get();
        user.setVerified(isVerified);
        user.setComment(comment);
        userRepository.save(user);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }
}
