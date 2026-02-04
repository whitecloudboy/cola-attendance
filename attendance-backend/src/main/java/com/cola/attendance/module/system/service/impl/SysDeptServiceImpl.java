package com.cola.attendance.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cola.attendance.module.system.dao.SysDeptDao;
import com.cola.attendance.module.system.dto.SysDeptDTO;
import com.cola.attendance.module.system.entity.SysDeptEntity;
import com.cola.attendance.module.system.service.SysDeptService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptDao, SysDeptEntity> implements SysDeptService {

    @Override
    public List<Long> getSubDeptIdList(Long deptId) {
        if (deptId == null) return List.of();
        List<SysDeptEntity> all = list();
        List<Long> ids = new java.util.ArrayList<>();
        collectSubIds(all, deptId, ids);
        return ids;
    }

    private void collectSubIds(List<SysDeptEntity> all, Long parentId, List<Long> out) {
        out.add(parentId);
        for (SysDeptEntity e : all) {
            if (parentId.equals(e.getParentId())) {
                collectSubIds(all, e.getId(), out);
            }
        }
    }

    @Override
    public List<SysDeptDTO> tree() {
        List<SysDeptEntity> all = list();
        return buildTree(all, null);
    }

    private List<SysDeptDTO> buildTree(List<SysDeptEntity> all, Long parentId) {
        return all.stream()
                .filter(e -> (parentId == null && e.getParentId() == null) || (parentId != null && parentId.equals(e.getParentId())))
                .map(e -> {
                    SysDeptDTO dto = new SysDeptDTO();
                    dto.setId(e.getId());
                    dto.setParentId(e.getParentId());
                    dto.setName(e.getName());
                    dto.setSort(e.getSort());
                    dto.setStatus(e.getStatus());
                    dto.setCreatedAt(e.getCreatedAt());
                    dto.setUpdatedAt(e.getUpdatedAt());
                    dto.setChildren(buildTree(all, e.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
