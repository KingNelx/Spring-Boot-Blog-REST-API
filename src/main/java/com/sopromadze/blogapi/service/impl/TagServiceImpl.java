package com.sopromadze.blogapi.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.sopromadze.blogapi.exception.ResourceNotFoundException;
import com.sopromadze.blogapi.model.role.RoleName;
import com.sopromadze.blogapi.model.tag.Tag;
import com.sopromadze.blogapi.payload.ApiResponse;
import com.sopromadze.blogapi.payload.PagedResponse;
import com.sopromadze.blogapi.repository.TagRepository;
import com.sopromadze.blogapi.security.UserPrincipal;
import com.sopromadze.blogapi.service.TagService;
import com.sopromadze.blogapi.utils.AppUtils;

@Service
public class TagServiceImpl implements TagService {
	
	@Autowired
	private TagRepository tagRepository;
	
	@Override
	public PagedResponse<Tag> getAllTags(int page, int size){
		AppUtils.validatePageNumberAndSize(page, size);
		
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		
		Page<Tag> tags = tagRepository.findAll(pageable);
		
		List<Tag> content = tags.getNumberOfElements() == 0 ? Collections.emptyList() : tags.getContent();
		
		return new PagedResponse<Tag>(content, tags.getNumber(), tags.getSize(), tags.getTotalElements(), tags.getTotalPages(), tags.isLast());
	}
	
	@Override
	public ResponseEntity<?> getTag(Long id){
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));
        return new ResponseEntity<>(tag, HttpStatus.OK);
    }
	
	@Override
	public ResponseEntity<?> addTag(Tag tag, UserPrincipal currentUser){
        Tag newTag =  tagRepository.save(tag);
        return new ResponseEntity<>(newTag, HttpStatus.CREATED);
    }
	
	@Override
	public ResponseEntity<?> updateTag(Long id, Tag newTag, UserPrincipal currentUser){
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));
        if (tag.getCreatedBy().equals(currentUser.getId()) || currentUser.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_ADMIN.toString()))){
            tag.setName(newTag.getName());
            Tag updatedTag = tagRepository.save(tag);
            return new ResponseEntity<>(updatedTag, HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse(Boolean.FALSE, "You don't have permission to edit this tag"), HttpStatus.UNAUTHORIZED);
    }
	
	@Override
	public ResponseEntity<?> deleteTag(Long id, UserPrincipal currentUser){
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));
        if (tag.getCreatedBy().equals(currentUser.getId()) || currentUser.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_ADMIN.toString()))){
            tagRepository.deleteById(id);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "You successfully deleted tag"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse(Boolean.FALSE, "You don't have permission to delete this tag"), HttpStatus.UNAUTHORIZED);
    }
}





















