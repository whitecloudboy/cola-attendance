package com.cola.attendance.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cola.attendance.module.system.dao.SysMenuDao;
import com.cola.attendance.module.system.dto.SysMenuDTO;
import com.cola.attendance.module.system.entity.SysMenuEntity;
import com.cola.attendance.module.system.service.SysMenuService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuDao, SysMenuEntity> implements SysMenuService {

    @Override
    public List<SysMenuDTO> tree() {
        List<SysMenuEntity> all = list();
        return buildTree(all, null);
    }

    private List<SysMenuDTO> buildTree(List<SysMenuEntity> all, Long parentId) {
        return all.stream()
                .filter(e -> (parentId == null && e.getParentId() == null) || (parentId != null && parentId.equals(e.getParentId())))
                .map(e -> {
                    SysMenuDTO dto = new SysMenuDTO();
                    dto.setId(e.getId());
                    dto.setParentId(e.getParentId());
                    dto.setName(e.getName());
                    dto.setPath(e.getPath());
                    dto.setComponent(e.getComponent());
                    dto.setType(e.getType());
                    dto.setPermission(e.getPermission());
                    dto.setIcon(e.getIcon());
                    dto.setSort(e.getSort());
                    dto.setVisible(e.getVisible());
                    dto.setCreatedAt(e.getCreatedAt());
                    dto.setChildren(buildTree(all, e.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
