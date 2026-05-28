package com.bookstore.service.user;

import com.bookstore.dto.user.request.AddressCreateRequest;
import com.bookstore.dto.user.request.AddressUpdateRequest;
import com.bookstore.dto.user.response.AddressResponse;
import com.bookstore.entity.user.Address;
import com.bookstore.entity.user.User;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.user.AddressMapper;
import com.bookstore.repository.user.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @Transactional(readOnly = true)
    public Page<AddressResponse> getAddressesByUser(User user, Pageable pageable) {
        return addressRepository.findAllByUser(user, pageable)
                .map(addressMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddress(UUID id, User user) {
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address with id " + id + " not found"));
        return addressMapper.toResponse(address);
    }

    @Transactional
    public AddressResponse createAddress(AddressCreateRequest request, User user) {
        Address address = addressMapper.toEntity(request);
        address.setUser(user);

        // Automatically make the first address the default
        if (!addressRepository.existsByUser(user)) {
            address.setIsDefault(true);
        }

        address = addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(UUID id, AddressUpdateRequest request, User user) {
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address with id " + id + " not found"));
        addressMapper.updateEntity(request, address);
        address = addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

    @Transactional
    public void deleteAddress(UUID id, User user) {
        Address addressToDelete = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address with id " + id + " not found"));

        boolean wasDefault = addressToDelete.getIsDefault();
        addressRepository.delete(addressToDelete);

        // Set another address as a default if the deleted address was the default
        if (wasDefault) {
            addressRepository.findFirstByUser(user)
                    .ifPresent(newDefault -> {
                        newDefault.setIsDefault(true);
                        addressRepository.save(newDefault);
                    });
        }
    }

    @Transactional
    public AddressResponse setDefaultAddress(UUID id, User user) {
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address with id " + id + " not found"));

        // Unset current default and set this one
        addressRepository.unsetCurrentDefault(user.getId());
        address.setIsDefault(true);
        address = addressRepository.save(address);

        return addressMapper.toResponse(address);
    }
}
