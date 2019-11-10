package ru.kireev.byteCodes.aop.instrumentation.proxy;


public class MyClassImpl {

  public void secureAccess(String param) {
    System.out.println("secureAccess, param:" + param);
  }

  @Override
  public String toString() {
    return "MyClassImpl{}";
  }
}
