package com.colacode.subject.domain.strategy;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SubjectTypeHandlerFactory {

    private final List<SubjectTypeHandler> handlerList;

    private final Map<Integer, SubjectTypeHandler> handlerMap = new HashMap<>();

    public SubjectTypeHandlerFactory(List<SubjectTypeHandler> handlerList) {
        this.handlerList = handlerList;
        for (SubjectTypeHandler handler : handlerList) {
            handlerMap.put(handler.getHandlerType(), handler);
        }
    }

    public SubjectTypeHandler getHandler(Integer subjectType) {
        return handlerMap.get(subjectType);
    }
}
