package de.schmiereck.projects.gnn;

import java.util.Objects;

public class Cell {
    private int pos;
    private Rule statusRule;
    private final Rules rules;

    public Cell(final RuleSet ruleSet) {
        this.rules = new Rules(ruleSet);
        this.statusRule = null;
    }

    public Cell(final RuleSet ruleSet, final Character status) {
        this.rules = new Rules(ruleSet);
        final Rule rule = this.rules.findRule(null, null, null);
        rule.outputStatus = status;
        this.statusRule = rule;
    }

    public int getPos() {
        return this.pos;
    }

    public void setPos(final int pos) {
        this.pos = pos;
    }

    public Rules getRules() {
        return this.rules;
    }

    public Rule getStatusRule() {
        return this.statusRule;
    }

    public Character getStatus() {
        return Objects.isNull(this.statusRule) ? null :this.statusRule.outputStatus;
    }

    public void setStatusRule(final Rule statusRule) {
        this.statusRule = statusRule;
    }

    public void setRule(final Character status, final Character c0, final Character c1, final Character c2) {
        rules.setRule(status, c0, c1, c2);
    }
}
