package com.buildbetter.plan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.plan.dto.tableau.TableauResponse;
import com.buildbetter.plan.service.TableauService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tableau")
@Slf4j
public class TableauController {
    private final TableauService tableauService;

    @GetMapping("")
    public TableauResponse[] getTableauData() {
        log.info("Tableau Controller : getTableauData");

        TableauResponse[] tableauResponse = tableauService.getTableauData();
        return tableauResponse;
    }
}
