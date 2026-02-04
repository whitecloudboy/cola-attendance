package com.cola.attendance.rule;

/**
 * 事件评估器接口，按序执行，必要时可短路。
 */
public interface IEventEvaluator {

    int getOrder();

    void evaluate(RuleContext context, RuleOutcome outcome);
}
