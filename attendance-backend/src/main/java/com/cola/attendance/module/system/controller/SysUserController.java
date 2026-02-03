package com.cola.attendance.module.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cola.attendance.common.result.Result;
import com.cola.attendance.module.system.dto.SysUserDTO;
import com.cola.attendance.module.system.entity.SysUserEntity;
import com.cola.attendance.module.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户")
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Operation(summary = "分页列表")
    @GetMapping("/page")
    public Result<Page<SysUserDTO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long deptId) {
        Page<SysUserEntity> page = Page.of(current, size);
        return Result.ok(sysUserService.pageDto(page, username, deptId));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public Result<SysUserDTO> get(@PathVariable Long id) {
        SysUserDTO dto = sysUserService.getByIdDto(id);
        return dto != null ? Result.ok(dto) : Result.fail("用户不存在");
    }

    @Operation(summary = "保存")
    @PostMapping
    public Result<Void> save(@RequestBody SysUserDTO dto) {
        sysUserService.saveUser(dto);
        return Result.ok(null);
    }

    @Operation(summary = "更新")
    @PutMapping
    public Result<Void> update(@RequestBody SysUserDTO dto) {
        sysUserService.updateUser(dto);
        return Result.ok(null);
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.ok(null);
    }
}
