package com.spj.sso.server.dao;

import com.spj.sso.server.pojo.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMapper {

    /**
     * 插入记录
     *
     */
    @Insert("INSERT INTO t_user (username, password) VALUES (#{username}, #{password})")
    int addUser(User user);

    /**
     *  查询
     *
     */
    @Select("SELECT * FROM t_user WHERE username=#{username} AND password=#{password}")
    User findUser(@Param("username") String username,@Param("password") String password);

    /**
     * 使用username查询
     *
     * @param id
     * @return
     */
    @Select("SELECT * FROM t_user WHERE username=#{username}")
    User findByName(@Param("username") String username);

}
