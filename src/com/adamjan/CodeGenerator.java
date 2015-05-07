package com.adamjan;

import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import java.util.List;

/**
 * Created by adam on 17.11.2014.
 */
public class CodeGenerator {
    private final PsiClass psiClass;
    private final List<PsiField> fields;
    private PsiElementFactory elementFactory;
    private JavaCodeStyleManager styleManager;

    public CodeGenerator(PsiClass psiClass, List<PsiField> fields) {
        this.psiClass = psiClass;
        this.fields = fields;
        elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        styleManager = JavaCodeStyleManager.getInstance(psiClass.getProject());
    }

    public void generate() {
        PsiClass classFromText = elementFactory.createClassFromText(generateBuilderClass(), psiClass);
        classFromText.setName("Builder");
        classFromText.getModifierList().setModifierProperty("public", true);
        classFromText.getModifierList().setModifierProperty("static", true);

        PsiMethod methodFromText = elementFactory.createMethodFromText(generateBuilderStaticMethod(), psiClass);


        styleManager.shortenClassReferences(psiClass.addBefore(methodFromText, psiClass.getLastChild()));
        addStepBuilderClasses(classFromText);
        styleManager.shortenClassReferences(psiClass.addBefore(classFromText, psiClass.getLastChild()));
    }

    private String generateBuilderStaticMethod() {
        return "public static Builder builder() {\n" +
                "        Builder builder = new Builder();\n" +
                "        builder.objectToBeBuild = new " + psiClass.getName() + "();\n" +
                "        return builder;\n" +
                "    }";
    }


    private void addStepBuilderClasses(PsiClass parentClass) {

        int i;
        for (i = 1; i < fields.size(); i++) {
            PsiClass partialClass = elementFactory.createClassFromText("private Builder" + i + "() {\n" +
                    "            }\n" +
                    "            public Builder" + (i + 1) + " " + fields.get(i).getName() + "(" + fields.get(i).getType().getPresentableText() + " " + fields.get(i).getName() + ") {\n" +
                    "                objectToBeBuild." + fields.get(i).getName() + " = " + fields.get(i).getName() + ";\n" +
                    "                return new Builder" + (i + 1) + "();\n" +
                    "            }", parentClass);
            partialClass.setName("Builder" + i);
            partialClass.getModifierList().setModifierProperty("public", true);
            parentClass.add(partialClass);
        }

        PsiClass lastPartialClass = elementFactory.createClassFromText(
                "            private Builder" + i + "() {\n" +
                        "            }\n" +
                        "            public " + psiClass.getName() + " build() {\n" +
                        "                return objectToBeBuild;\n" +
                        "            }\n" +
                        "        }", parentClass);
        lastPartialClass.setName("Builder" + i);
        lastPartialClass.getModifierList().setModifierProperty("public", true);
        parentClass.add(lastPartialClass);
    }

    private String generateBuilderClass() {
        return "        private " + psiClass.getName() + " objectToBeBuild;\n" +
                "        private Builder() {\n" +
                "        }\n" +
                "        public Builder1 " + fields.get(0).getName() + "(" + fields.get(0).getType().getPresentableText() + " " + fields.get(0).getName() + ") {\n" +
                "            objectToBeBuild." + fields.get(0).getName() + " = " + fields.get(0).getName() + ";\n" +
                "            return new Builder1();\n" +
                "        }";
    }
}