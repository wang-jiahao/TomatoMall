package com.example.tomatomall.serviceImpl;

import com.example.tomatomall.po.User;
import com.example.tomatomall.repository.UserRepository;
import com.example.tomatomall.service.UserService;
import com.example.tomatomall.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {
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
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOptional.get();
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
        User userPO = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        // 更新允许修改的字段
        if (userVO.getName() != null) userPO.setName(userVO.getName());
        if (userVO.getPassword() != null) {
            userPO.setPassword(passwordEncoder.encode(userVO.getPassword()));
        }
        // 其他字段更新类似...
        return userRepository.save(userPO);
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