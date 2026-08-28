package com.sportzone.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sportzone.user.dto.AddressDTO;
import com.sportzone.user.entity.UserAddress;
import com.sportzone.user.mapper.UserAddressMapper;
import com.sportzone.user.service.AddressService;
import com.sportzone.user.vo.AddressVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    private final UserAddressMapper addressMapper;

    public AddressServiceImpl(UserAddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public void addAddress(Long userId, AddressDTO dto) {
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefaultAddress(userId);
        }

        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setReceiverName(dto.getReceiverName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setAddress(dto.getAddress());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        addressMapper.insert(address);
    }

    @Override
    public void updateAddress(Long userId, Long addressId, AddressDTO dto) {
        UserAddress address = getAddress(userId, addressId);

        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefaultAddress(userId);
        }

        address.setReceiverName(dto.getReceiverName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setAddress(dto.getAddress());
        if (dto.getIsDefault() != null) {
            address.setIsDefault(dto.getIsDefault());
        }
        addressMapper.updateById(address);
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        UserAddress address = getAddress(userId, addressId);
        addressMapper.deleteById(addressId);
    }

    @Override
    public List<AddressVO> getAddressList(Long userId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId).orderByDesc(UserAddress::getIsDefault).orderByDesc(UserAddress::getCreateTime);
        List<UserAddress> addresses = addressMapper.selectList(wrapper);
        return addresses.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public AddressVO getAddressById(Long userId, Long addressId) {
        UserAddress address = getAddress(userId, addressId);
        return convertToVO(address);
    }

    @Override
    public void setDefaultAddress(Long userId, Long addressId) {
        UserAddress address = getAddress(userId, addressId);
        clearDefaultAddress(userId);
        address.setIsDefault(1);
        addressMapper.updateById(address);
    }

    private UserAddress getAddress(Long userId, Long addressId) {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getId, addressId).eq(UserAddress::getUserId, userId);
        UserAddress address = addressMapper.selectOne(wrapper);
        if (address == null) {
            throw new RuntimeException("地址不存在");
        }
        return address;
    }

    private void clearDefaultAddress(Long userId) {
        LambdaUpdateWrapper<UserAddress> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId).eq(UserAddress::getIsDefault, 1);
        UserAddress address = new UserAddress();
        address.setIsDefault(0);
        addressMapper.update(address, wrapper);
    }

    private AddressVO convertToVO(UserAddress address) {
        AddressVO vo = new AddressVO();
        BeanUtils.copyProperties(address, vo);
        return vo;
    }
}