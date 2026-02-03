package com.cola.attendance.module.system.controller;

import com.cola.attendance.common.result.Result;
import com.cola.attendance.module.system.dto.SysDeptDTO;
import com.cola.attendance.module.system.entity.SysDeptEntity;
import com.cola.attendance.module.system.service.SysDeptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门")
@RestController
@RequestMapping("/system/dept")
public class SysDeptController {

    private final SysDeptService sysDeptService;

    public SysDeptController(SysDeptService sysDeptService) {
        this.sysDeptService = sysDeptService;
    }

    @Operation(summary = "部门树")
    @GetMapping("/tree")
    public Result<List<SysDeptDTO>> tree() {
        return Result.ok(sysDeptService.tree());
    }

    @Operation(summary = "分页列表")
    @GetMapping("/page")
    public Result<Page<SysDeptEntity>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name) {
        LambdaQueryWrapper<SysDeptEntity> q = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            q.like(SysDeptEntity::getName, name);
        }
        q.orderByAsc(SysDeptEntity::getSort);
        return Result.ok(sysDeptService.page(Page.of(current, size), q));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public Result<SysDeptEntity> get(@PathVariable Long id) {
        return Result.ok(sysDeptService.getById(id));
    }

    @Operation(summary = "保存")
    @PostMapping
    public Result<Void> save(@RequestBody SysDeptEntity entity) {
        sysDeptService.save(entity);
        return Result.ok(null);
    }

    @Operation(summary = "更新")
    @PutMapping
    public Result<Void> update(@RequestBody SysDeptEntity entity) {
        sysDeptService.updateById(entity);
        return Result.ok(null);
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysDeptService.removeById(id);
        return Result.ok(null);
    }
}
