package ru.kireev.byteCodes.aop.instrumentation.changer;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * @author Andrey Kieev
 */
public class Agent {
  public static void premain(String agentArgs, Instrumentation inst) {
    System.out.println("premain");
    inst.addTransformer(new ClassFileTransformer() {
      @Override
      public byte[] transform(ClassLoader loader, String className,
                              Class<?> classBeingRedefined,
                              ProtectionDomain protectionDomain,
                              byte[] classfileBuffer) {
        if (className.equals("ru/kireev/byteCodes/aop/instrumentation/changer/AnyClass")) {
          return changeMethod(classfileBuffer);
        }
        return classfileBuffer;
      }
    });

  }

  private static byte[] changeMethod(byte[] originalClass) {
    ClassReader cr = new ClassReader(originalClass);
    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
    ClassVisitor cv = new ClassVisitor(Opcodes.ASM7, cw) {
      @Override
      public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (name.equals("summator")) {
          return new ChangeMethodVisitor(methodVisitor, access, name, descriptor);
        } else {
          return methodVisitor;
        }
      }
    };
    cr.accept(cv, Opcodes.ASM7);

    byte[] finalClass = cw.toByteArray();

    try (OutputStream fos = new FileOutputStream("changer.class")) {
      fos.write(finalClass);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return finalClass;
  }

  private static class ChangeMethodVisitor extends AdviceAdapter {
    ChangeMethodVisitor(MethodVisitor methodVisitor, int access, String name, String descriptor) {
      super(Opcodes.ASM7, methodVisitor, access, name, descriptor);
    }

    @Override
    public void visitInsn(final int opcode) {
      if (IADD == opcode) {
        System.out.println("replace IADD to ISUB");
        super.visitInsn(ISUB);
      } else {
        super.visitInsn(opcode);
      }

    }
  }
}
