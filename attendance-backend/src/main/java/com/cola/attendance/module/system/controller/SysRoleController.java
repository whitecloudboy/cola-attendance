package com.cola.attendance.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cola.attendance.common.result.Result;
import com.cola.attendance.module.system.entity.SysRoleEntity;
import com.cola.attendance.module.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "角色")
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @Operation(summary = "分页列表")
    @GetMapping("/page")
    public Result<Page<SysRoleEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name) {
        LambdaQueryWrapper<SysRoleEntity> q = new LambdaQueryWrapper<>();
        if (code != null && !code.isBlank()) q.eq(SysRoleEntity::getCode, code);
        if (name != null && !name.isBlank()) q.like(SysRoleEntity::getName, name);
        q.orderByDesc(SysRoleEntity::getId);
        return Result.ok(sysRoleService.page(Page.of(current, size), q));
    }

    @Operation(summary = "列表（不分页）")
    @GetMapping("/list")
    public Result<java.util.List<SysRoleEntity>> list() {
        return Result.ok(sysRoleService.list());
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public Result<SysRoleEntity> get(@PathVariable Long id) {
        return Result.ok(sysRoleService.getById(id));
    }

    @Operation(summary = "保存")
    @PostMapping
    public Result<Void> save(@RequestBody SysRoleEntity entity) {
        sysRoleService.save(entity);
        return Result.ok(null);
    }

    @Operation(summary = "更新")
    @PutMapping
    public Result<Void> update(@RequestBody SysRoleEntity entity) {
        sysRoleService.updateById(entity);
        return Result.ok(null);
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.removeById(id);
        return Result.ok(null);
    }
}
