package com.example.tomatomall.serviceImpl;

import com.example.tomatomall.po.User;
import com.example.tomatomall.repository.UserRepository;
import com.example.tomatomall.service.UserService;
import com.example.tomatomall.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl implements UserService {
//    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User register(UserVO userVO) {
        // 校验用户名唯一性
        if (userRepository.findByUsername(userVO.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        // 校验手机号和邮箱格式
        validatePhone(userVO.getTelephone());
        validateEmail(userVO.getEmail());

        // VO → PO 转换
        User userPO = new User();
        BeanUtils.copyProperties(userVO, userPO);
        userPO.setPassword(passwordEncoder.encode(userVO.getPassword()));
        return userRepository.save(userPO);
    }

    @Override
    public User authenticate(String username, String password) {
//        System.out.println("[Debug] Authenticating user: " + username);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
//            System.out.println("[Debug] User not found: " + username);
            throw new RuntimeException("用户不存在");
        }

        User user = userOptional.get();

//        System.out.println("[Debug] Stored password hash: " + user.getPassword());
//        System.out.println("[Debug] Input password: " + password);
        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
//        System.out.println("[Debug] Password matches: " + passwordMatches);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    @Override
    public UserVO getUserVOByUsername(String username) {
        User userPO = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        // PO → VO 转换
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userPO, userVO);
        return userVO;
    }

    @Override
    public User updateUser(String username, UserVO userVO) {
//        logger.debug("接收到的更新请求体: {}", userVO.toString());
        try {
//            logger.debug("开始更新用户: {}", username);
            User userPO = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 更新字段并记录日志
            if (userVO.getName() != null) {
                userPO.setName(userVO.getName());
//                logger.debug("更新用户姓名: {}", userVO.getName());
            }
            if (userVO.getPassword() != null) {
                String encodedPassword = passwordEncoder.encode(userVO.getPassword());
                userPO.setPassword(encodedPassword);
//                logger.debug("更新用户密码（哈希后）: {}", encodedPassword);
            }
            if (userVO.getEmail() != null) {
                validateEmail(userVO.getEmail()); // 邮箱格式校验
                userPO.setEmail(userVO.getEmail());
//                logger.debug("更新用户邮箱: {}", userVO.getEmail());
            }
            // 其他字段更新...

            User updatedUser = userRepository.save(userPO);
//            logger.info("用户更新成功: {}", username);
            return updatedUser;
        } catch (DataIntegrityViolationException e) {
//            logger.error("数据库约束冲突: {}", e.getMessage());
            throw new RuntimeException("邮箱或电话已存在");
        } catch (Exception e) {
//            logger.error("更新用户失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新失败: " + e.getMessage());
        }
    }

    // 私有校验方法
    private void validatePhone(String phone) {
        if (phone != null && !Pattern.matches("^1\\d{10}$", phone)) {
            throw new RuntimeException("手机号格式错误");
        }
    }

    private void validateEmail(String email) {
        if (email != null && !Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", email)) {
            throw new RuntimeException("邮箱格式错误");
        }
    }
}