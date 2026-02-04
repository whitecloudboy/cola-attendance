package com.cola.attendance.module.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cola.attendance.common.result.Result;
import com.cola.attendance.module.system.dto.SysPostDTO;
import com.cola.attendance.module.system.entity.SysPostEntity;
import com.cola.attendance.module.system.service.SysPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "岗位")
@RestController
@RequestMapping("/system/post")
public class SysPostController {

    private final SysPostService sysPostService;

    public SysPostController(SysPostService sysPostService) {
        this.sysPostService = sysPostService;
    }

    @Operation(summary = "列表（按部门、排序）")
    @GetMapping("/list")
    public Result<List<SysPostDTO>> list(@RequestParam(required = false) Long deptId) {
        return Result.ok(sysPostService.listDto(deptId));
    }

    @Operation(summary = "分页")
    @GetMapping("/page")
    public Result<Page<SysPostDTO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long deptId) {
        return Result.ok(sysPostService.pageDto(Page.of(current, size), name, deptId));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public Result<SysPostEntity> get(@PathVariable Long id) {
        return Result.ok(sysPostService.getById(id));
    }

    @Operation(summary = "保存")
    @PostMapping
    public Result<Void> save(@RequestBody SysPostEntity entity) {
        sysPostService.save(entity);
        return Result.ok(null);
    }

    @Operation(summary = "更新")
    @PutMapping
    public Result<Void> update(@RequestBody SysPostEntity entity) {
        sysPostService.updateById(entity);
        return Result.ok(null);
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysPostService.removeById(id);
        return Result.ok(null);
    }
}
