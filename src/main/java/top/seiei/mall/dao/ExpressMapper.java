package top.seiei.mall.dao;

import top.seiei.mall.bean.Express;

import java.util.List;

public interface ExpressMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Express record);

    int insertSelective(Express record);

    Express selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Express record);

    int updateByPrimaryKey(Express record);

    List<Express> selectByOrderNo(Long orderNo);
}