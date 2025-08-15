package com.ruoyi.project.qiksearch.controller;

import com.ruoyi.framework.aspectj.lang.annotation.Anonymous;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.qiksearch.service.IQikSearchService;
import com.ruoyi.project.system.domain.SysUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 搜索相关接口
 * 符合RuoYi风格：继承BaseController，使用AjaxResult返回，添加Sw用Api注解
 */
@RestController
@RequestMapping("/qs")
public class QikSearchController extends BaseController {

    @Autowired
    private IQikSearchService qikSearchService;

    @GetMapping("/search")
    public TableDataInfo fullTextSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize) throws IOException {
        return qikSearchService.search(keyword, pageNum, pageSize);
    }

    @PostMapping("/add")
    public AjaxResult addQuestion(@RequestParam String questionContent) throws IOException {
        boolean success = qikSearchService.addQuestion(questionContent);
        if (success) {
            return AjaxResult.success("添加成功");
        } else {
            return AjaxResult.error("添加失败");
        }
    }
}
