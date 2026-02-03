package com.cola.attendance.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cola.attendance.common.result.Result;
import com.cola.attendance.module.system.dto.SysMenuDTO;
import com.cola.attendance.module.system.entity.SysMenuEntity;
import com.cola.attendance.module.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单")
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    private final SysMenuService sysMenuService;

    public SysMenuController(SysMenuService sysMenuService) {
        this.sysMenuService = sysMenuService;
    }

    @Operation(summary = "菜单树")
    @GetMapping("/tree")
    public Result<List<SysMenuDTO>> tree() {
        return Result.ok(sysMenuService.tree());
    }

    @Operation(summary = "分页列表")
    @GetMapping("/page")
    public Result<Page<SysMenuEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name) {
        LambdaQueryWrapper<SysMenuEntity> q = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) q.like(SysMenuEntity::getName, name);
        q.orderByAsc(SysMenuEntity::getSort);
        return Result.ok(sysMenuService.page(Page.of(current, size), q));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public Result<SysMenuEntity> get(@PathVariable Long id) {
        return Result.ok(sysMenuService.getById(id));
    }

    @Operation(summary = "保存")
    @PostMapping
    public Result<Void> save(@RequestBody SysMenuEntity entity) {
        sysMenuService.save(entity);
        return Result.ok(null);
    }

    @Operation(summary = "更新")
    @PutMapping
    public Result<Void> update(@RequestBody SysMenuEntity entity) {
        sysMenuService.updateById(entity);
        return Result.ok(null);
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysMenuService.removeById(id);
        return Result.ok(null);
    }
}
