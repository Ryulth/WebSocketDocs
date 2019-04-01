package com.ryulth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.pojo.model.Account;
import com.ryulth.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AccountController {
    private static Logger logger = LoggerFactory.getLogger(AccountController.class);
    @Autowired
    AccountService accountService;
    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;
    @CrossOrigin("*")
    @PostMapping("/docs/{docsId}/accounts")
    public void docsLogin(@PathVariable Long docsId, @RequestBody Account account , HttpServletRequest request){
        account.setRemoteAddress(request.getRemoteAddr());
        accountService.setAccount(docsId,account);
    }
    @CrossOrigin("*")
    @GetMapping("/docs/{docsId}/accounts")
    public String docsAccounts(@PathVariable Long docsId) throws JsonProcessingException {
        return accountService.getAccounts(docsId);
    }
//    @CrossOrigin("*")
//    @DeleteMapping("/docs/{docsId}/accounts/{clientSessionId}")
//    public void docsLogout(@PathVariable("docsId") Long docsId,@PathVariable("clientSessionId") String clientSessionId, HttpServletRequest request){
//        Account deleteAccount = Account.builder().clientSessionId(clientSessionId).remoteAddress(request.getRemoteAddr()).build();
//        accountService.deleteAccount(docsId,deleteAccount);
//    }

    @EventListener
    @Async
    public void handleWebsocketconnectListner(SessionConnectEvent event) throws JsonProcessingException {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        logger.info("session open : " + sha.getSessionId());
        this.simpMessagingTemplate.convertAndSend("/topic/docs/" + accountService.getDocsId(sha.getSessionId()),
                accountService.getAccountsBySessionId(sha.getSessionId()));
    }
    @EventListener
    @Async
    public void handleWebsocketDisconnectListner(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        logger.info("session closed : " + sha.getSessionId());
        accountService.deleteAccount(sha.getSessionId());
    }
}