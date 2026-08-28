package com.sportzone.user.service;

import com.sportzone.user.dto.AddressDTO;
import com.sportzone.user.vo.AddressVO;
import java.util.List;

public interface AddressService {

    void addAddress(Long userId, AddressDTO dto);

    void updateAddress(Long userId, Long addressId, AddressDTO dto);

    void deleteAddress(Long userId, Long addressId);

    List<AddressVO> getAddressList(Long userId);

    AddressVO getAddressById(Long userId, Long addressId);

    void setDefaultAddress(Long userId, Long addressId);
}