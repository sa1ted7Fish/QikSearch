package com.ruoyi.project.qiksearch.controller;

import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.qiksearch.service.IQikSearchDevService;
import com.ruoyi.project.qiksearch.service.IQikSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 搜索相关接口
 * 符合RuoYi风格：继承BaseController，使用AjaxResult返回，添加Sw用Api注解
 */
@RestController
@RequestMapping("/qs-dev")
public class QikSearchDevController extends BaseController {

    @Autowired
    private IQikSearchDevService qikSearchDevService;

    @GetMapping("/search")
    public TableDataInfo fullTextSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize) throws IOException {
        return qikSearchDevService.search(keyword, pageNum, pageSize);
    }
}
