package com.thaiopensource.relaxng.impl;

public class RequiredContent {
  private static class RequiredContentFunction extends AbstractPatternFunction {
    protected final PatternBuilder spb;

    RequiredContentFunction(PatternBuilder spb) {
      this.spb = spb;
    }

    public Object caseAfter(AfterPattern p) {
      // we get only the content, not anything after.
      if (p.getOperand1().isNullable()) {
        return spb.makeEmpty();
      }
      return p.getOperand1().applyForPattern(this);
    }

    public Object caseChoice(ChoicePattern p) {
      if (p.isNullable()) {
        return spb.makeEmpty();
      } else if (p.getOperand1().isNullable()) {
        return p.getOperand2().applyForPattern(this);
      } else if (p.getOperand2().isNullable()) {
        return p.getOperand1().applyForPattern(this);
      } else {
        return spb.makeChoice(p.getOperand1().applyForPattern(this), 
            p.getOperand2().applyForPattern(this));
      }
    }

    public Object caseGroup(GroupPattern p) {
      if (p.isNullable()) {
        return spb.makeEmpty();
      } else if (p.getOperand1().isNullable()) {
        return p.getOperand2().applyForPattern(this);
      } else if (p.getOperand2().isNullable()) {
        return p.getOperand1().applyForPattern(this);
      } else {
        return spb.makeGroup(p.getOperand1().applyForPattern(this), 
            p.getOperand2().applyForPattern(this));
      }
    }

    public Object caseInterleave(InterleavePattern p) {
      if (p.isNullable()) {
        return spb.makeEmpty();
      } else if (p.getOperand1().isNullable()) {
        return p.getOperand2().applyForPattern(this);
      } else if (p.getOperand2().isNullable()) {
        return p.getOperand1().applyForPattern(this);
      } else {
        return spb.makeInterleave(p.getOperand1().applyForPattern(this), 
            p.getOperand2().applyForPattern(this));

      }
    }

    public Object caseOneOrMore(OneOrMorePattern p) {
      if (p.isNullable()) {
        return spb.makeEmpty();
      }
      return spb.makeOneOrMore(p.getOperand().applyForPattern(this));
    }

    public Object caseRef(RefPattern p) {
      // not clear if we should check to avoid going into an infinite loop...
      if (p.isNullable()) {
        return spb.makeEmpty();
      }
      return p.getPattern().applyForPattern(this);
    }
    
    public Object caseOther(Pattern p) {
      if (p.isNullable()) {
        return spb.makeEmpty();
      }
      return p;
    }
  }

  private static class RequiredFrontierContentFunction extends RequiredContent.RequiredContentFunction {

    RequiredFrontierContentFunction(PatternBuilder spb) {
      super(spb);
    }

    public Object caseGroup(GroupPattern p) {
      if (p.isNullable()) {
        return spb.makeEmpty();
      } else if (p.getOperand1().isNullable()) {
        return p.getOperand2().applyForPattern(this);
      } else {
        return p.getOperand1().applyForPattern(this);
      }
    }
  }
 
  private static class RequiredAttributesFunction extends RequiredContent.RequiredContentFunction {

    RequiredAttributesFunction(PatternBuilder spb) {
      super(spb);
    }

    public Object caseElement(ElementPattern p) {
      return spb.makeEmpty();
    }
  }
  
  public static Pattern getRequiredContent(PatternBuilder spb, Pattern p) {
    return p.applyForPattern(new RequiredContentFunction(spb));
  }
  
  public static Pattern getRequiredFrontierContent(PatternBuilder spb, Pattern p) {
    return p.applyForPattern(new RequiredFrontierContentFunction(spb));
  }
  
  public static Pattern getRequiredAttributes(PatternBuilder spb, Pattern p) {
    return p.applyForPattern(new RequiredAttributesFunction(spb));
  }
  
  
}
