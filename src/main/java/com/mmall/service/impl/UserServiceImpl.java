package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import com.mmall.util.RedisUtil;
import com.mmall.util.TokenCache;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 描述：用户模块业务实现类
 * 作者：NearJC
 * 时间：2019.4.24
 */
@Service("userService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public ServerResponse<User> login(String username, String password) {
        // 判断用户名是否为空
        if (StringUtils.isEmpty(username)) {
            return ServerResponse.createByErrorMessage("用户名不允许为空");
        }

        // 判断密码是否为空
        if (StringUtils.isEmpty(password)) {
            return ServerResponse.createByErrorMessage("密码不允许为空");
        }

        // 校验用户名是否存在
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        // 登录密码MD5加密
        password = MD5Util.MD5Encode(password);

        // 验证用户名和密码
        User user = userMapper.selectLogin(username, password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }

        // 处理返回的密码为空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    // 用户注册
    @Override
    public ServerResponse<String> register(User user) {
        // 校验用户名是否为空
        String username = user.getUsername();
        if (StringUtils.isEmpty(username)) {
            return ServerResponse.createByErrorMessage("用户名不允许为空");
        }

        // 校验密码是否为空
        String password = user.getPassword();
        if (StringUtils.isEmpty(password)) {
            return ServerResponse.createByErrorMessage("密码不允许为空");
        }

        // 校验邮箱是否为空
        String email = user.getEmail();
        if (StringUtils.isEmpty(email)) {
            return ServerResponse.createByErrorMessage("邮箱不允许为空");
        }

        // 校验手机号是否为空
        String phone = user.getPhone();
        if (StringUtils.isEmpty(phone)) {
            return ServerResponse.createByErrorMessage("手机号码不允许为空");
        }

        // 校验邮箱格式
        String emailRegex = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        if (!Pattern.matches(emailRegex, email)) {
            return ServerResponse.createByErrorMessage("邮箱格式不正确");
        }

        // 校验手机号格式
        String phoneRegex = "^1([38]\\d|5[0-35-9]|7[3678])\\d{8}$";
        if (!Pattern.matches(phoneRegex, phone)) {
            return ServerResponse.createByErrorMessage("手机号格式不正确");
        }

        // 校验用户名是否存在
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }

        // 校验email是否已注册
        validResponse = this.checkValid(email, Const.EMAIL);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }

        // 设置用户注册为普通用户
        user.setRole(Const.Role.ROLE_CUSTOMER);

        // MD5加密
        user.setPassword(MD5Util.MD5Encode(password));

        // 添加用户信息
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        } else {
            return ServerResponse.createBySuccessMessage("注册成功");
        }
    }

    /**
     * 用户名或邮箱验证
     *
     * @param parameter
     * @param type
     * @return
     */
    @Override
    public ServerResponse<String> checkValid(String parameter, String type) {
        // 参数非空校验
        if (StringUtils.isEmpty(parameter) || StringUtils.isEmpty(type)) {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        // 校验用户名是否存在
        if (Const.USERNAME.equals(type)) {
            int resultCount = userMapper.checkUsername(parameter);
            if (resultCount > 0) {
                return ServerResponse.createByErrorMessage("用户名已存在");
            }

            return ServerResponse.createBySuccessMessage("检测成功");
        }

        // 校验邮箱是否存在
        if (Const.EMAIL.equals(type)) {
            int resultCount = userMapper.checkEmail(parameter);
            if (resultCount > 0) {
                return ServerResponse.createByErrorMessage("邮箱已被注册");
            }

            return ServerResponse.createBySuccessMessage("检测成功");
        }

        return ServerResponse.createByErrorMessage("检测类型不存在");
    }

    /**
     * 根据用户名获取提示问题
     *
     * @param username
     * @return
     */
    @Override
    public ServerResponse<String> getQuestion(String username) {
        // 检测用户名是否为空
        if (StringUtils.isEmpty(username)) {
            return ServerResponse.createByErrorMessage("用户名不允许为空");
        }

        // 检测用户是否存在
        ServerResponse checkNameResponse = checkValid(username, Const.USERNAME);
        if (checkNameResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        // 获取提示问题
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }

        return ServerResponse.createByErrorMessage("用户未设置提示问题，请联系管理员修改密码");
    }

    /**
     * 检测答案与提示问题是否匹配
     *
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        // 检测用户名是否为空
        if (StringUtils.isEmpty(username)) {
            return ServerResponse.createByErrorMessage("用户名不允许为空");
        }

        // 检测提示问题是否为空
        if (StringUtils.isEmpty(question)) {
            return ServerResponse.createByErrorMessage("提示问题不允许为空");
        }

        // 检测答案是否为空
        if (StringUtils.isEmpty(answer)) {
            return ServerResponse.createByErrorMessage("答案不允许为空");
        }

        // 检测用户是否存在
        ServerResponse checkNameResponse = checkValid(username, Const.USERNAME);
        if (checkNameResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        // 检测答案与提示问题是否匹配
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            // 生成Token，修改密码时需要带回来
            String forgetToken = UUID.randomUUID().toString();
            // 保存到redis
            RedisUtil.setex(Const.RedisCache.TOKEN_PREFIX + username, forgetToken, 60 * 60 * 12);

            return ServerResponse.createBySuccess(forgetToken);
        }

        return ServerResponse.createByErrorMessage("答案与提示问题不匹配");
    }

    /**
     * 未登录状态下重置密码
     *
     * @param forgetToken token值
     * @param username
     * @param newPassword
     * @return
     */
    @Override
    public ServerResponse forgetResetPassword(String forgetToken, String username, String newPassword) {

        // 检测用户名是否为空
        if (StringUtils.isBlank(username)) {
            return ServerResponse.createByErrorMessage("用户名不允许为空");
        }

        // 检测token值是否为空
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }

        // 检测密码是否为空
        if (StringUtils.isBlank(newPassword)) {
            return ServerResponse.createByErrorMessage("密码不允许为空");
        }

        // 检测用户是否存在
        ServerResponse checkNameResponse = checkValid(username, Const.USERNAME);
        if (checkNameResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        // 获取保存的token值
        String localToken = RedisUtil.get(Const.RedisCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(localToken)) {
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }

        // 判断本地token与用户传递的token是否一致
        if (StringUtils.equals(forgetToken, localToken)) {
            // 修改密码
            newPassword = MD5Util.MD5Encode(newPassword);
            int resultCount = userMapper.updatePasswordByUsername(username, newPassword);
            if (resultCount > 0) {
                return ServerResponse.createBySuccess("修改密码成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }

        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    /**
     * 重置密码
     *
     * @param oldPassword
     * @param newPassword
     * @param user
     * @return
     */
    @Override
    public ServerResponse resetPassword(String oldPassword, String newPassword, User user) {

        // 判断旧密码是否为空
        if (StringUtils.isBlank(oldPassword)) {
            return ServerResponse.createByErrorMessage("旧密码不允许为空");
        }

        // 判断新密码是否为空
        if (StringUtils.isBlank(newPassword)) {
            return ServerResponse.createByErrorMessage("新密码不允许为空");
        }

        // 判断旧密码是否正确
        int resultCount = userMapper.checkPassword(MD5Util.MD5Encode(oldPassword), user.getId());
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        // 修改密码
        user.setPassword(MD5Util.MD5Encode(newPassword));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createByErrorMessage("密码修改成功");
        }

        return ServerResponse.createBySuccess("密码修改失败");
    }

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public ServerResponse updateInformation(User user) {
        // 校验手机号是否为空
        String phone = user.getPhone();
        if (StringUtils.isEmpty(phone)) {
            return ServerResponse.createByErrorMessage("手机号码不允许为空");
        }

        // 校验邮箱是否为空
        String email = user.getEmail();
        if (StringUtils.isEmpty(email)) {
            return ServerResponse.createByErrorMessage("邮箱不允许为空");
        }

        // 校验手机号格式
        String phoneRegex = "^1([38]\\d|5[0-35-9]|7[3678])\\d{8}$";
        if (!Pattern.matches(phoneRegex, phone)) {
            return ServerResponse.createByErrorMessage("手机号格式不正确");
        }

        // 校验邮箱格式
        String emailRegex = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        if (!Pattern.matches(emailRegex, email)) {
            return ServerResponse.createByErrorMessage("邮箱格式不正确");
        }

        // 检测邮箱是否被注册，除去自己本身的
        if (StringUtils.isNotBlank(email)) {
            int resultCount = userMapper.checkEmailByUserId(user.getId(), user.getEmail());
            if (resultCount < 0) {
                return ServerResponse.createByErrorMessage("邮箱已经存在");
            }
        }

        // 修改信息
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            updateUser.setUsername(user.getUsername());
            return ServerResponse.createBySuccess("更新个人信息成功", updateUser);
        }

        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    // -----------------------后台业务----------------------------

    /**
     * 校验是否是管理员
     *
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> checkAdminRole(User user) {

        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        } else {
            return ServerResponse.createByError();
        }
    }


}