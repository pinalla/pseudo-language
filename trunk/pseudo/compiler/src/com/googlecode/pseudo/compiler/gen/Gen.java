package com.googlecode.pseudo.compiler.gen;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;

import javax.tools.JavaFileManager;
import javax.tools.SimpleJavaFileObject;
import javax.tools.JavaFileObject.Kind;

import code.googlecode.pseudo.compiler.model.Record;
import code.googlecode.pseudo.compiler.model.Script;
import code.googlecode.pseudo.compiler.model.Functions.UserFunction;
import code.googlecode.pseudo.compiler.model.Vars.ParameterVar;

import com.googlecode.pseudo.compiler.LocationMap;
import com.googlecode.pseudo.compiler.Type;
import com.googlecode.pseudo.compiler.LocationMap.Location;
import com.googlecode.pseudo.compiler.Types.ArrayType;
import com.googlecode.pseudo.compiler.Types.FunType;
import com.googlecode.pseudo.compiler.Types.PrimitiveType;
import com.googlecode.pseudo.compiler.analysis.ErrorReporter;
import com.googlecode.pseudo.compiler.analysis.Invocation;
import com.googlecode.pseudo.compiler.analysis.TypeCheck;
import com.googlecode.pseudo.compiler.ast.ArrayAccessId;
import com.googlecode.pseudo.compiler.ast.ArrayAccessPrimary;
import com.googlecode.pseudo.compiler.ast.Assignation;
import com.googlecode.pseudo.compiler.ast.Block;
import com.googlecode.pseudo.compiler.ast.ConditionalIf;
import com.googlecode.pseudo.compiler.ast.ConditionalIfElse;
import com.googlecode.pseudo.compiler.ast.DeclarationId;
import com.googlecode.pseudo.compiler.ast.DeclarationIdInit;
import com.googlecode.pseudo.compiler.ast.Expr;
import com.googlecode.pseudo.compiler.ast.ExprBooleanLiteral;
import com.googlecode.pseudo.compiler.ast.ExprCharLiteral;
import com.googlecode.pseudo.compiler.ast.ExprId;
import com.googlecode.pseudo.compiler.ast.ExprNullLiteral;
import com.googlecode.pseudo.compiler.ast.ExprPrimary;
import com.googlecode.pseudo.compiler.ast.ExprStringLiteral;
import com.googlecode.pseudo.compiler.ast.ExprValueLiteral;
import com.googlecode.pseudo.compiler.ast.FieldAccessId;
import com.googlecode.pseudo.compiler.ast.FieldAccessPrimary;
import com.googlecode.pseudo.compiler.ast.ForLoopIncr;
import com.googlecode.pseudo.compiler.ast.ForLoopIncrAssignation;
import com.googlecode.pseudo.compiler.ast.ForLoopIncrFuncall;
import com.googlecode.pseudo.compiler.ast.ForLoopInit;
import com.googlecode.pseudo.compiler.ast.ForLoopInitAssignation;
import com.googlecode.pseudo.compiler.ast.ForLoopInitDeclaration;
import com.googlecode.pseudo.compiler.ast.ForLoopInitFuncall;
import com.googlecode.pseudo.compiler.ast.FuncallId;
import com.googlecode.pseudo.compiler.ast.FuncallPrimary;
import com.googlecode.pseudo.compiler.ast.FunctionDef;
import com.googlecode.pseudo.compiler.ast.FunctionRtype;
import com.googlecode.pseudo.compiler.ast.IdToken;
import com.googlecode.pseudo.compiler.ast.InstrAssignation;
import com.googlecode.pseudo.compiler.ast.InstrBlock;
import com.googlecode.pseudo.compiler.ast.InstrBreak;
import com.googlecode.pseudo.compiler.ast.InstrConditional;
import com.googlecode.pseudo.compiler.ast.InstrContinue;
import com.googlecode.pseudo.compiler.ast.InstrDeclaration;
import com.googlecode.pseudo.compiler.ast.InstrEmpty;
import com.googlecode.pseudo.compiler.ast.InstrFuncall;
import com.googlecode.pseudo.compiler.ast.InstrLoop;
import com.googlecode.pseudo.compiler.ast.InstrPrint;
import com.googlecode.pseudo.compiler.ast.InstrReturn;
import com.googlecode.pseudo.compiler.ast.InstrScan;
import com.googlecode.pseudo.compiler.ast.InstrThrow;
import com.googlecode.pseudo.compiler.ast.Lhs;
import com.googlecode.pseudo.compiler.ast.LhsArrayAccess;
import com.googlecode.pseudo.compiler.ast.LhsFieldAccess;
import com.googlecode.pseudo.compiler.ast.LhsId;
import com.googlecode.pseudo.compiler.ast.LoopDowhile;
import com.googlecode.pseudo.compiler.ast.LoopFor;
import com.googlecode.pseudo.compiler.ast.LoopLabel;
import com.googlecode.pseudo.compiler.ast.LoopWhile;
import com.googlecode.pseudo.compiler.ast.Node;
import com.googlecode.pseudo.compiler.ast.Parameter;
import com.googlecode.pseudo.compiler.ast.ParameterTyped;
import com.googlecode.pseudo.compiler.ast.Parameters;
import com.googlecode.pseudo.compiler.ast.PrimaryArrayAccess;
import com.googlecode.pseudo.compiler.ast.PrimaryFieldAccess;
import com.googlecode.pseudo.compiler.ast.PrimaryFuncall;
import com.googlecode.pseudo.compiler.ast.PrimaryPrimaryNoArrayCreation;
import com.googlecode.pseudo.compiler.ast.RecordDef;
import com.googlecode.pseudo.compiler.ast.RecordInit;
import com.googlecode.pseudo.compiler.ast.ScriptMember;
import com.googlecode.pseudo.compiler.ast.ScriptMemberBlock;
import com.googlecode.pseudo.compiler.ast.ScriptMemberFunctionDef;
import com.googlecode.pseudo.compiler.ast.ScriptMemberRecordDef;
import com.googlecode.pseudo.compiler.ast.Start;
import com.googlecode.pseudo.compiler.ast.Visitor;
import com.googlecode.pseudo.compiler.parser.PseudoProductionEnum;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Todo;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.Pretty;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Position;

public class Gen extends Visitor<JCTree, GenEnv, RuntimeException> {
  private final Script script;
  private final ErrorReporter errorReporter;
  private final LocationMap locationMap;
  private final TypeCheck typeCheck;
  
  private final Context context;
  private final SimpleJavaFileObject sourceFile;
  private final TreeMaker maker;
  private final Names names;
  
  private final HashSet<String> functionLiteralSet =
    new HashSet<String>();
  private final ListBuffer<JCTree> functionLiteralBuffer =
    ListBuffer.lb();
  
  private static final String RUNTIME_CLASS = "com.googlecode.pseudo.runtime.Runtimes";
  
  public Gen(final Script script, ErrorReporter errorReporter, LocationMap locationMap, TypeCheck typeCheck) {
    this.script = script;
    this.errorReporter = errorReporter;
    this.locationMap = locationMap;
    this.typeCheck = typeCheck;
    
    this.context=new Context();
    
    // gen generate indy bytecode
    Options.instance(context).put("invokedynamic",  "invokedynamic");
    
    // fake source object, needed by enter pass
    sourceFile = new SimpleJavaFileObject(URI.create(""), Kind.OTHER) {
      @Override
      public Reader openReader(boolean ignoreEncodingErrors) {
        throw new UnsupportedOperationException();
      }
      @Override
      public Writer openWriter() {
        throw new UnsupportedOperationException();
      }
      
      @Override
      public String toString() {
        return script.getScriptName();
      }
    };
    
    // create a redirection log
    new RedirectionLog(context, sourceFile, errorReporter);
    
    // if fileManager not already set, register the JavacFileManager to be used
    if (context.get(JavaFileManager.class) == null)
        JavacFileManager.preRegister(context);
    
    this.maker=TreeMaker.instance(context);
    this.names=Names.instance(context);
  }
  
  // --- 
  
  Name nameFromString(String name) {
    return names.fromString(name);
  }
  
  JCExpression identifier(Node node, String name) {
    return maker(node).Ident(nameFromString(name));
  }
  
  JCExpression qualifiedIdentifier(Node node, String name) {
    String[] names = name.split("\\.");
    JCExpression type = identifier(node, names[0]);
    for(int i=1; i<names.length; i++) {
      type = maker(node).Select(type, nameFromString(names[i]));
    }
    return type;
  }
  
  JCExpression asType(Node node, Type type) {
    if (type instanceof PrimitiveType) {
      switch((PrimitiveType)type) {
      case BOOLEAN:
        return maker(node).TypeIdent(TypeTags.BOOLEAN);
      case CHAR:
        return maker(node).TypeIdent(TypeTags.CHAR);
      case INT:
        return maker(node).TypeIdent(TypeTags.INT);
      case DOUBLE:
        return maker(node).TypeIdent(TypeTags.DOUBLE);
      case STRING:
        return qualifiedIdentifier(node, "java.lang.String");
      case VOID:
        return maker(node).TypeIdent(TypeTags.VOID);  
      case NULL:
      case ANY:
        return qualifiedIdentifier(node, "java.lang.Object");
      }
      throw new AssertionError("unknown primitive type "+type);
    }
    if (type instanceof ArrayType) {
      return maker(node).TypeArray(asType(node, ((ArrayType)type).getComponentType()));
    }
    if (type instanceof FunType) {
      return qualifiedIdentifier(node, "java.dyn.MethodHandle");
    }
    if (type instanceof Record) {
      Record record = (Record)type;
      return identifier(node, record.getName());
    }
    throw new AssertionError("unknown type "+type);
  }
  
  JCExpression asBoxedType(Node node, Type type) {
    if (type instanceof PrimitiveType) {
      switch((PrimitiveType)type) {
      case BOOLEAN:
        return qualifiedIdentifier(node, "java.lang.Boolean");
      case CHAR:
        return qualifiedIdentifier(node, "java.lang.Character");
      case INT:
        return qualifiedIdentifier(node, "java.lang.Integer");
      case DOUBLE:
        return qualifiedIdentifier(node, "java.lang.Double");
      case STRING:
        return qualifiedIdentifier(node, "java.lang.String");
      case ANY:
        return qualifiedIdentifier(node, "java.lang.Object");
      default:
      }
      throw new AssertionError("invalid primitive type "+type);
    }
    return asType(node, type);
  }
  
  private JCExpression asType(Node node) {
    Type type = typeCheck.getTypeMap().get(node);
    if (type == null)
      throw new AssertionError("no type for node "+node);
    return asType(node, type);
  }
  
  private JCModifiers modifiers(Node node, long flags) {
    return maker(node).Modifiers(flags);
  }
  
  // --- javac tree maker
  
  TreeMaker maker(Node node) {
    Location location = locationMap.getLocation(node);
    if (location == null) {
      throw new AssertionError("no location for node "+node.getClass().getName());
    }
    
    return maker.at(Position.encodePosition(1+location.getLine(), 1+location.getColumn()));
  }
  
  
  // -- dynamic cast
  
  private JCExpression retype(Node lhsNode, Node rhsNode, JCExpression expr) {
    Type lhsType = typeCheck.getTypeMap().get(lhsNode);
    Type rhsType = typeCheck.getTypeMap().get(rhsNode);
    
    return retype(lhsType, rhsNode, rhsType, expr);
  }
  
  private JCExpression retype(Type lhsType, Node rhsNode, Type rhsType, JCExpression expr) {
    if (lhsType == null || rhsType == null)
      throw new AssertionError("retype "+lhsType+" "+rhsType);
    
    if (lhsType != PrimitiveType.ANY && rhsType == PrimitiveType.ANY) {
      // dynamic cast
      JCFieldAccess method = maker(rhsNode).Select(qualifiedIdentifier(rhsNode, "java.dyn.InvokeDynamic"), nameFromString("__cast__"));
      return maker(rhsNode).Apply(List.of(asType(rhsNode, lhsType)), method, List.of(expr));
    }
    return expr;
  }
  
  
  
  // --- helpers
  
  private JCTree gen(Node node, GenEnv genEnv) {
    return node.accept(this, genEnv);
  }
  
  private <T extends JCTree> T gen(Node node, Class<T> tClass, GenEnv genEnv) {
    return tClass.cast(gen(node, genEnv));
  }
  
  
  private <T extends JCTree> List<T> genAllSubNodes(java.util.List<? extends Node> nodeList, Class<T> tClass) {
    return genAllSubNodes(nodeList, tClass, Collections.<Type>nCopies(nodeList.size(), PrimitiveType.VOID));
  }
  
  private <T extends JCTree> List<T> genAllSubNodes(java.util.List<? extends Node> nodeList, Class<T> tClass, java.util.List<Type> expectedTypes) {
    assert nodeList.size() == expectedTypes.size();
    
    ListBuffer<T> buffer = ListBuffer.lb();
    Iterator<Type> expectedTypeIt = expectedTypes.iterator();
    for(Node subNode:nodeList) {
      Type expectedType = expectedTypeIt.next();
      JCTree result = gen(subNode, new GenEnv(expectedType));
      if (result != null)
        buffer.append(tClass.cast(result));
    }
    return buffer.toList();
  }
  
  
  
  // --- default visit
  
  /*@Override
  protected JCTree visit(Node node, GenEnv genEnv) {
    genAllSubNodes(node, genEnv);
    return null;
  }*/
  
// --- entry point
  
  public void gen(Start start) throws IOException {
    JavaCompiler compiler=new JavaCompiler(context);
    Todo todo=Todo.instance(context);
    
    JCCompilationUnit compilationUnit=genCompilationUnit(start);
    compilationUnit.sourcefile=sourceFile;
    
    // enter, attribute and flow
    compiler.enterTrees(List.of(compilationUnit));
    
    //System.out.println("todo "+todo);
    Queue<Env<AttrContext>> queue = compiler.flow(compiler.attribute(todo));
    
    // report errors
    compiler.reportDeferredDiagnostics();
    
    // debug
    if (errorReporter.isOnError()) {
      OutputStreamWriter writer = new OutputStreamWriter(System.out);
      try {
        new Pretty(writer, false).printUnit(compilationUnit, null);
        writer.flush();
      } finally {
        writer.close();
      }
    }
    
    // generate code if no error
    if (!errorReporter.isOnError()) {
      compiler.generate(compiler.desugar(queue));
    }
  }
  
  private JCCompilationUnit genCompilationUnit(Start start) {
    GenEnv genEnv = new GenEnv(null);
    
    // gen records and functions
    ListBuffer<JCTree> memberBuffer = ListBuffer.lb(); 
    for(ScriptMember scriptMember:start.getScriptMemberStar()) {
      JCTree member = gen(scriptMember, genEnv);
      if (member == null) { // if it's a block, skip it
        continue;
      }
      memberBuffer.append(member);
    }
    
    // gen main block
    JCBlock mainBlock = gen(script.getMainBlock(), JCBlock.class, genEnv);
    
    JCVariableDecl args = maker(start).VarDef(modifiers(start, Flags.FINAL),
        nameFromString("ARGS"),
        asType(start, new ArrayType(PrimitiveType.STRING)),
        null);
    
    JCMethodDecl main = maker(start).MethodDef(modifiers(start, Flags.PUBLIC|Flags.STATIC|Flags.FINAL),
        nameFromString("main"),
        asType(start, PrimitiveType.VOID),
        List.<JCTypeParameter>nil(),
        List.of(args),
        List.of(qualifiedIdentifier(start, "java.lang.Throwable")),
        mainBlock,
        null);
    memberBuffer.append(main);
    memberBuffer.appendList(functionLiteralBuffer);
    
    JCFieldAccess runtimesClass = maker(start).Select(qualifiedIdentifier(start, RUNTIME_CLASS), names._class);
    JCStatement registerBootstrapInstr = maker(start).Exec(
        maker(start).Apply(List.<JCExpression>nil(),
            maker(start).Select(qualifiedIdentifier(start, "java.dyn.Linkage"),
                nameFromString("registerBootstrapMethod")),
                           List.<JCExpression>of(runtimesClass,
                               maker(start).Literal(
                               TypeTags.CLASS,
                               "bootstrapMethod"))));
    JCBlock staticBlock = maker(start).Block(Flags.STATIC, List.of(registerBootstrapInstr));
    memberBuffer.append(staticBlock);
    
    JCClassDecl topLevelClass = maker(start).ClassDef(modifiers(start, 0),
        nameFromString(script.getScriptName()),
        List.<JCTypeParameter>nil(),
        null,
        List.<JCExpression>nil(),
        memberBuffer.toList());
    
    return maker(start).TopLevel(List.<JCAnnotation>nil(), null, List.<JCTree>of(topLevelClass));
  }
  
  
  
  // ---
  
  
  // --- top level members
  
  @Override
  public JCTree visit(ScriptMemberRecordDef scriptMemberRecordDef, GenEnv genEnv) {
    return gen(scriptMemberRecordDef.getRecordDef(), genEnv);
  }
  @Override
  public JCTree visit(ScriptMemberFunctionDef scriptMemberFunctionDef, GenEnv genEnv) {
    return gen(scriptMemberFunctionDef.getFunctionDef(), genEnv);
  }
  @Override
  public JCTree visit(ScriptMemberBlock scriptMemberBlock, GenEnv param) {
    // member blocks are generated via the main block
    return null;
  }
  
  @Override
  public JCTree visit(RecordDef recordDef, GenEnv  genEnv) {
    String recordName = recordDef.getId().getValue();
    
    JCModifiers mods=maker(recordDef).Modifiers(0);
    Name name=nameFromString(recordName);
    List<JCTree> members = genAllSubNodes(recordDef.getFieldStar(), JCTree.class);
    
    // generate init
    Record record = script.getRecordTable().lookup(recordName);
    RecordInit initOptional = recordDef.getRecordInitOptional();
    if (initOptional == null) {
      // generate default init
      // TODO
    } else {
      JCTree initFunction = genUserFunction(initOptional, record.getInitFunction(), initOptional.getParameters(), null, genEnv);
      members = members.prepend(initFunction);
    }
    
    return maker(recordDef).ClassDef(mods, name, List.<JCTypeParameter>nil(), null, List.<JCExpression>nil(), members);
  }
  
  private JCTree genUserFunction(Node functionNode, UserFunction function, Parameters parameters, /*maybenull*/Node returnTypeNode, GenEnv genEnv) {
    JCModifiers mods=modifiers(functionNode, Flags.PUBLIC|Flags.STATIC);
    Name name=nameFromString(function.getName());
    
    FunType functionType = function.getType();
    
    JCExpression returnType = asType(returnTypeNode, functionType.getReturnType());
    
    ListBuffer<JCVariableDecl> parameterBuffer = ListBuffer.lb();
    Iterator<Parameter> parameterIterator = parameters.getParameterStar().iterator();
    for(ParameterVar parameterVar:function.getParameterTable().items()) {
      Parameter parameter = parameterIterator.next();
      parameterBuffer.append(genParameter(parameter, parameterVar));
    }
    
    JCBlock block = gen(function.getBlock(), JCBlock.class, genEnv);
    
    return maker(functionNode).MethodDef(mods, name,
        returnType,
        List.<JCTypeParameter>nil(),
        parameterBuffer.toList(),
        List.of(qualifiedIdentifier(functionNode, "java.lang.Throwable")),
        block,
        null);
  }
  
  @Override
  public JCTree visit(FunctionDef functionDef, GenEnv genEnv) {
    String functionName = functionDef.getId().getValue();
    UserFunction function = script.getFunctionScope().getTable().lookup(functionName);
    FunctionRtype functionRtypeOptional = functionDef.getFunctionRtypeOptional();
    Node returnTypeNode = (functionRtypeOptional==null)?functionDef:functionRtypeOptional;
    return genUserFunction(functionDef, function, functionDef.getParameters(), returnTypeNode, genEnv);
  }
  
  private JCVariableDecl genParameter(Parameter parameter, ParameterVar parameterVar) {
    Name name = nameFromString(parameterVar.getName());
    Node typeNode = (parameter instanceof ParameterTyped)?((ParameterTyped)parameter).getId():parameter;
    return maker(parameter).VarDef(modifiers(parameter, Flags.FINAL),
        name, asType(typeNode, parameterVar.getType()), null);
  }
  
  
  // --- instructions
  
  @Override
  public JCTree visit(Block block, GenEnv genEnv) {
    List<JCStatement> instructions = genAllSubNodes(block.getInstrStar(), JCStatement.class);
    return maker(block).Block(0, instructions);
  }
  
  @Override
  public JCTree visit(InstrBlock instrBlock, GenEnv genEnv) {
    return gen(instrBlock.getBlock(), genEnv);
  }
  
  @Override
  public JCTree visit(InstrDeclaration instrDeclaration, GenEnv genEnv) {
    return gen(instrDeclaration.getDeclaration(), genEnv);
  }
  @Override
  public JCTree visit(InstrAssignation instrAssignation, GenEnv genEnv) {
    return gen(instrAssignation.getAssignation(), genEnv);
  }
  
  @Override
  public JCTree visit(InstrBreak instrBreak, GenEnv genEnv) {
    Name label = null;
    IdToken idOptional = instrBreak.getIdOptional();
    if (idOptional != null) {
      label = nameFromString(idOptional.getValue());
    }
    return maker(instrBreak).Break(label);
  }
  @Override
  public JCTree visit(InstrContinue instrContinue, GenEnv genEnv) {
    Name label = null;
    IdToken idOptional = instrContinue.getIdOptional();
    if (idOptional != null) {
      label = nameFromString(idOptional.getValue());
    }
    return maker(instrContinue).Continue(label);
  }
  
  @Override
  public JCTree visit(InstrReturn instrReturn, GenEnv genEnv) {
    JCExpression expr = null;
    Expr exprOptional = instrReturn.getExprOptional();
    if (exprOptional != null) {
      // get expected function return type
      Type functionReturnType = typeCheck.getTypeMap().get(instrReturn);
      expr = gen(exprOptional, JCExpression.class, new GenEnv(functionReturnType));
      
      expr = retype(instrReturn, exprOptional, expr);
    }
    return maker(instrReturn).Return(expr);
  }
  
  @Override
  public JCTree visit(InstrEmpty instrEmpty, GenEnv genEnv) {
    return null;  // genAllSubNodes filter out null
  }
  
  
  // --- 
  
  @Override
  public JCTree visit(InstrPrint instrPrint, GenEnv genEnv) {
    JCExpression expr = gen(instrPrint.getExpr(), JCExpression.class, genEnv);
    JCFieldAccess println = maker(instrPrint).Select(qualifiedIdentifier(instrPrint, "System.out"), nameFromString("println"));
    return maker(instrPrint).Exec(
        maker(instrPrint).Apply(List.<JCExpression>nil(), println, List.of(expr)));
  }
  
  @Override
  public JCTree visit(InstrScan instrScan, GenEnv genEnv) {
    //TODO
    throw new UnsupportedOperationException();
  }
  
  @Override
  public JCTree visit(InstrThrow instrThrow, GenEnv genEnv) {
    Expr exprNode = instrThrow.getExpr();
    JCExpression expr = gen(exprNode, JCExpression.class, genEnv);
    
    JCNewClass newClass = maker(exprNode).NewClass(null, List.<JCExpression>nil(),
        qualifiedIdentifier(exprNode, "java.lang.AssertionError"),
        List.of(expr),
        null);
    return maker(instrThrow).Throw(newClass);
  }
  
  // --- conditional
  
  @Override
  public JCTree visit(InstrConditional instrConditional, GenEnv genEnv) {
    return gen(instrConditional.getConditional(), genEnv);
  }
  @Override
  public JCTree visit(ConditionalIf conditionalIf, GenEnv genEnv) {
    JCExpression condition = gen(conditionalIf.getExpr(), JCExpression.class, genEnv);
    JCBlock block = gen(conditionalIf.getBlock(), JCBlock.class, genEnv);
    
    return maker(conditionalIf).If(condition, block, null);
  }
  @Override
  public JCTree visit(ConditionalIfElse conditionalIfElse, GenEnv genEnv) {
    JCExpression condition = gen(conditionalIfElse.getExpr(), JCExpression.class, genEnv);
    JCBlock thenPart = gen(conditionalIfElse.getBlock(), JCBlock.class, genEnv);
    JCBlock elsePart = gen(conditionalIfElse.getBlock2(), JCBlock.class, genEnv);
    
    return maker(conditionalIfElse).If(condition, thenPart, elsePart);
  }
  
  
  // --- loop
  
  @Override
  public JCTree visit(InstrLoop instrLoop, GenEnv genEnv) {
    JCStatement loop = gen(instrLoop.getLoop(), JCStatement.class, genEnv);
    
    LoopLabel labelOptional = instrLoop.getLoopLabelOptional();
    if (labelOptional != null) {
      String label = labelOptional.getId().getValue();
      return maker(labelOptional).Labelled(nameFromString(label), loop);
    }
    return loop;
  }
  
  @Override
  public JCTree visit(LoopDowhile loopDowhile, GenEnv genEnv) {
    JCBlock block = gen(loopDowhile.getBlock(), JCBlock.class, genEnv); 
    JCExpression condition = gen(loopDowhile.getExpr(), JCExpression.class, genEnv);
    
    return maker(loopDowhile).DoLoop(block, condition);
  }
  
  @Override
  public JCTree visit(LoopWhile loopWhile, GenEnv genEnv) {
    JCBlock block = gen(loopWhile.getBlock(), JCBlock.class, genEnv); 
    JCExpression condition = gen(loopWhile.getExpr(), JCExpression.class, genEnv);
    
    return maker(loopWhile).WhileLoop(condition, block);
  }
  
  @Override
  public JCTree visit(LoopFor loopFor, GenEnv genEnv) {
    JCStatement init = null;
    ForLoopInit initOptional = loopFor.getForLoopInitOptional();
    if (initOptional != null) {
      init = gen(initOptional, JCStatement.class, genEnv);
    }
    
    JCExpression condition = null;
    Expr exprOptional = loopFor.getExprOptional();
    if (exprOptional != null) {
      condition = gen(exprOptional, JCExpression.class, genEnv);
    }
    
    JCExpressionStatement incr = null;
    ForLoopIncr incrOptional = loopFor.getForLoopIncrOptional();
    if (incrOptional != null) {
      incr = gen(incrOptional, JCExpressionStatement.class, genEnv);
    }
     
    JCBlock block = gen(loopFor.getBlock(), JCBlock.class, genEnv); 
    
    return maker(loopFor).ForLoop(List.of(init), condition, List.of(incr), block);
  }
  
  @Override
  public JCTree visit(ForLoopInitAssignation forLoopInitAssignation, GenEnv genEnv) {
    return gen(forLoopInitAssignation.getAssignation(), genEnv);
  }
  @Override
  public JCTree visit(ForLoopInitDeclaration forLoopInitDeclaration, GenEnv genEnv) {
    return gen(forLoopInitDeclaration.getDeclaration(), genEnv);
  }
  @Override
  public JCTree visit(ForLoopInitFuncall forLoopInitFuncall, GenEnv genEnv) {
    return gen(forLoopInitFuncall.getFuncall(), genEnv);
  }
  
  @Override
  public JCTree visit(ForLoopIncrAssignation forLoopIncrAssignation, GenEnv genEnv) {
    return gen(forLoopIncrAssignation.getAssignation(), genEnv);
  }
  @Override
  public JCTree visit(ForLoopIncrFuncall forLoopIncrFuncall, GenEnv genEnv) {
    JCExpression expr = gen(forLoopIncrFuncall.getFuncall(), JCExpression.class, genEnv);
    // loop incrment is an expression statement
    return maker(forLoopIncrFuncall).Exec(expr);
  }
  
  // --- declaration/assignation
  
  @Override
  public JCTree visit(DeclarationId declarationId, GenEnv genEnv) {
    String name = declarationId.getId().getValue();
    return maker(declarationId).VarDef(modifiers(declarationId, 0),
        nameFromString(name),
        asType(declarationId),
        null);
  }
  
  @Override
  public JCTree visit(DeclarationIdInit declarationIdInit, GenEnv genEnv) {
    JCExpression expr = gen(declarationIdInit.getExpr(), JCExpression.class, genEnv);
    String name = declarationIdInit.getId().getValue();
    return maker(declarationIdInit).VarDef(modifiers(declarationIdInit, 0),
        nameFromString(name),
        asType(declarationIdInit),
        retype(declarationIdInit, declarationIdInit.getExpr(), expr));
  }
  
  @Override
  public JCTree visit(Assignation assignation, GenEnv genEnv) {
    Lhs lhsNode = assignation.getLhs();
    JCExpression lhs = gen(lhsNode, JCExpression.class, genEnv);
    JCExpression rhs = gen(assignation.getExpr(), JCExpression.class, genEnv);
    
    // declaration ?
    if (typeCheck.getAutoDeclarationSet().contains(lhsNode)) {
      String name = ((LhsId)lhsNode).getId().getValue();
      return maker(assignation).VarDef(modifiers(assignation, 0),
          nameFromString(name),
          asType(assignation, PrimitiveType.ANY),
          rhs);
    }
    
    // assignation (assignation is a statement, not a expression)
    return maker(assignation).Exec(
        maker(assignation).Assign(lhs, retype(lhsNode, assignation.getExpr(), rhs)));
  }
  
  
  // --- lhs
  
  @Override
  public JCTree visit(LhsId lhsId, GenEnv genEnv) {
    String name = lhsId.getId().getValue();
    return maker(lhsId).Ident(nameFromString(name));
  }
  
  @Override
  public JCTree visit(LhsArrayAccess lhsArrayAccess, GenEnv genEnv) {
    return gen(lhsArrayAccess.getArrayAccess(), genEnv);
  }
  @Override
  public JCTree visit(PrimaryArrayAccess primaryArrayAccess, GenEnv genEnv) {
    return gen(primaryArrayAccess.getArrayAccess(), genEnv);
  }
  @Override
  public JCTree visit(ArrayAccessId arrayAccessId, GenEnv genEnv) {
    JCExpression array = gen(arrayAccessId.getId(), JCExpression.class, genEnv);
    JCExpression index = gen(arrayAccessId.getExpr(), JCExpression.class, genEnv);
    return maker(arrayAccessId).Indexed(array, index);
  }
  @Override
  public JCTree visit(ArrayAccessPrimary arrayAccessPrimary, GenEnv genEnv) {
    JCExpression array = gen(arrayAccessPrimary.getPrimaryNoArrayCreation(), JCExpression.class, genEnv);
    JCExpression index = gen(arrayAccessPrimary.getExpr(), JCExpression.class, genEnv);
    return maker(arrayAccessPrimary).Indexed(array, index);
  }
  
  @Override
  public JCTree visit(LhsFieldAccess lhsFieldAccess, GenEnv genEnv) {
    return gen(lhsFieldAccess.getFieldAccess(), genEnv);
  }
  @Override
  public JCTree visit(PrimaryFieldAccess primaryFieldAccess, GenEnv genEnv) {
    return gen(primaryFieldAccess.getFieldAccess(), genEnv);
  }
  @Override
  public JCTree visit(FieldAccessId fieldAccessId, GenEnv genEnv) {
    JCExpression selected = identifier(fieldAccessId.getId(), fieldAccessId.getId().getValue());
    return maker(fieldAccessId).Select(selected, nameFromString(fieldAccessId.getId2().getValue()));
  }
  @Override
  public JCTree visit(FieldAccessPrimary fieldAccessPrimary, GenEnv genEnv) {
    JCExpression selected = gen(fieldAccessPrimary.getPrimary(), JCExpression.class, genEnv);
    return maker(fieldAccessPrimary).Select(selected, nameFromString(fieldAccessPrimary.getId().getValue()));
  }
  
  // --- primary
  
  @Override
  public JCTree visit(PrimaryPrimaryNoArrayCreation primaryPrimaryNoArrayCreation, GenEnv genEnv) {
    return gen(primaryPrimaryNoArrayCreation.getPrimaryNoArrayCreation(), genEnv);
  }
  
  // --- funcall
  
  @Override
  public JCTree visit(PrimaryFuncall primaryFuncall, GenEnv genEnv) {
    return gen(primaryFuncall.getFuncall(), genEnv);
  }
  @Override
  public JCTree visit(InstrFuncall instrFuncall, GenEnv genEnv) {
    return maker(instrFuncall).Exec(gen(instrFuncall.getFuncall(), JCExpression.class, genEnv));
  }
  
  @Override
  public JCTree visit(FuncallId funcallId, GenEnv genEnv) {
    Invocation invocation = typeCheck.getInvocationMap().get(funcallId);
    FunType funType = invocation.getFunType();
    
    // try to adjust the return type to the expected return type
    Type returnType = funType.getReturnType();
    Type expectedReturnType = genEnv.getExpectedReturnType();
    if (returnType == PrimitiveType.ANY && expectedReturnType != PrimitiveType.ANY) {
      returnType = expectedReturnType;
      // fix computed type
      typeCheck.getTypeMap().put(funcallId, returnType);
    }
    
    List<JCExpression> exprs = genAllSubNodes(funcallId.getArguments().getExprStar(), JCExpression.class, funType.getParameterTypes());
    
    String name = funcallId.getId().getValue();
    if (invocation.getFunction() == null) {
      JCExpression funExpr = identifier(funcallId.getId(), name);
      //funExpr = maker(funcallId).TypeCast(qualifiedIdentifier(funcallId, "java.dyn.MethodHandle"), funExpr);
      exprs = exprs.prepend(funExpr);
      
      JCFieldAccess method = maker(funcallId).Select(qualifiedIdentifier(funcallId, "java.dyn.InvokeDynamic"), nameFromString("__call__"));
      return maker(funcallId).Apply(List.of(asType(funcallId, returnType)), method, exprs);
    } else {
      return maker(funcallId).Apply(List.<JCExpression>nil(), identifier(funcallId, name), exprs);
    }
  }
  
  @Override
  public JCTree visit(FuncallPrimary funcallPrimary, GenEnv genEnv) {
    Invocation invocation = typeCheck.getInvocationMap().get(funcallPrimary);
    FunType funType = invocation.getFunType();
    
    // try to adjust the return type to the expected return type
    Type returnType = funType.getReturnType();
    Type expectedReturnType = genEnv.getExpectedReturnType();
    if (returnType == PrimitiveType.ANY && expectedReturnType != PrimitiveType.ANY) {
      returnType = expectedReturnType;
      // fix computed type
      typeCheck.getTypeMap().put(funcallPrimary, returnType);
    }
    
    JCExpression funExpr = gen(funcallPrimary.getPrimary(), JCExpression.class, genEnv);
    //funExpr = maker(funcallPrimary).TypeCast(qualifiedIdentifier(funcallPrimary, "java.dyn.MethodHandle"), funExpr);
    
    List<JCExpression> exprs = genAllSubNodes(funcallPrimary.getArguments().getExprStar(), JCExpression.class, funType.getParameterTypes());
    exprs = exprs.prepend(funExpr);
    
    JCFieldAccess method = maker(funcallPrimary).Select(qualifiedIdentifier(funcallPrimary, "java.dyn.InvokeDynamic"), nameFromString("__call__"));
    return maker(funcallPrimary).Apply(List.of(asType(funcallPrimary, returnType)), method, exprs);
  }
  
  // --- expression
  
  @Override
  public JCTree visit(ExprBooleanLiteral exprBooleanLiteral, GenEnv unused) {
    return maker(exprBooleanLiteral).Literal(
        TypeTags.BOOLEAN,
        exprBooleanLiteral.getBooleanLiteral().getValue());
  }
  
  @Override
  public JCTree visit(ExprCharLiteral exprCharLiteral, GenEnv unused) {
    return maker(exprCharLiteral).Literal(
        TypeTags.CHAR,
        exprCharLiteral.getCharLiteral().getValue());
  }
  
  @Override
  public JCTree visit(ExprValueLiteral exprValueLiteral, GenEnv unused) {
    Object value = exprValueLiteral.getValueLiteral().getValue();
    return maker(exprValueLiteral).Literal((value instanceof Integer)?TypeTags.INT:TypeTags.DOUBLE,value);
  }
  
  @Override
  public JCTree visit(ExprStringLiteral exprStringLiteral, GenEnv unused) {
    return maker(exprStringLiteral).Literal(
        TypeTags.CLASS,
        exprStringLiteral.getStringLiteral().getValue());
  }
  
  @Override
  public JCTree visit(ExprNullLiteral exprNullLiteral, GenEnv unused) {
    return maker(exprNullLiteral).Literal(
        TypeTags.BOT,
        null);
  }
  
  private JCExpression asClassLiteral(Node node, Type type) {
    return maker(node).Select(asType(node, type), names._class);
  }
  
  @Override
  public JCTree visit(ExprId exprId, GenEnv genEnv) {
    String name = exprId.getId().getValue();
    Type type = typeCheck.getTypeMap().get(exprId);
    if (type instanceof FunType && !functionLiteralSet.contains(name)) {
      FunType funType = (FunType)type;
      
      JCFieldAccess lookup = maker(exprId).Select(
          qualifiedIdentifier(exprId, "java.dyn.MethodHandles"),
          nameFromString("lookup"));
      JCExpression lookupApply = maker(exprId).Apply(List.<JCExpression>nil(), lookup, List.<JCExpression>nil());
      
      
      ListBuffer<JCExpression> argsBuffer = ListBuffer.lb();
      argsBuffer.append(asClassLiteral(exprId, funType.getReturnType()));
      for(Type parameterType: funType.getParameterTypes()) {
        argsBuffer.append(asClassLiteral(exprId, parameterType));
      }
      
      JCFieldAccess methodTypeMake = maker(exprId).Select(
          qualifiedIdentifier(exprId, "java.dyn.MethodType"),
          nameFromString("make"));
      JCExpression methodTypeMakeApply = maker(exprId).Apply(List.<JCExpression>nil(), methodTypeMake, argsBuffer.toList());
      
      JCFieldAccess findStatic = maker(exprId).Select(
          lookupApply,
          nameFromString("findStatic"));
      
      JCFieldAccess currentClass = maker(exprId).Select(identifier(exprId, script.getScriptName()), names._class);
      JCLiteral nameLit = maker(exprId).Literal(TypeTags.CLASS,name);
      JCExpression init = maker(exprId).Apply(List.<JCExpression>nil(), findStatic, List.of(currentClass, nameLit, methodTypeMakeApply));
      
      JCTree functionLiteral = maker(exprId).VarDef(
          modifiers(exprId, Flags.PRIVATE|Flags.STATIC|Flags.FINAL),
          nameFromString(name),
          qualifiedIdentifier(exprId, "java.dyn.MethodHandle"),
          init);
      
      functionLiteralBuffer.append(functionLiteral);
      functionLiteralSet.add(name);
    }
    return maker(exprId).Ident(nameFromString(name));
  }
  
  @Override
  public JCTree visit(ExprPrimary exprPrimary, GenEnv genEnv) {
    return gen(exprPrimary.getPrimary(), genEnv);
  }
  
  /*
  @Override
  public JCTree visit(ExprEq exprEq, GenEnv genEnv) {
    JCExpression left = gen(exprEq.getExpr(), JCExpression.class, genEnv);
    JCExpression right = gen(exprEq.getExpr2(), JCExpression.class, genEnv);
    return maker(exprEq).Binary(JCTree.EQ, left, right);
  }
  @Override
  public JCTree visit(ExprNe exprNe, GenEnv genEnv) {
    JCExpression left = gen(exprNe.getExpr(), JCExpression.class, genEnv);
    JCExpression right = gen(exprNe.getExpr2(), JCExpression.class, genEnv);
    return maker(exprNe).Binary(JCTree.NE, left, right);
  }*/
  
  static class Operator {
    final int opcode;
    final String name;
    
    public Operator(int opcode, String name) {
      this.opcode = opcode;
      this.name = name;
    }
  }
  
  private static final EnumMap<PseudoProductionEnum, Operator> exprOperatorMap;
  static {
    EnumMap<PseudoProductionEnum, Operator> map =
      new EnumMap<PseudoProductionEnum, Operator>(PseudoProductionEnum.class);
    
    // unary
    map.put(PseudoProductionEnum.expr_neg, new Operator(JCTree.NEG, "!"));
    map.put(PseudoProductionEnum.expr_unary_minus, new Operator(JCTree.MINUS, "-"));
    map.put(PseudoProductionEnum.expr_unary_plus, new Operator(JCTree.PLUS, "+"));
    
    // binary
    map.put(PseudoProductionEnum.expr_eq, new Operator(JCTree.EQ, "=="));
    map.put(PseudoProductionEnum.expr_ne, new Operator(JCTree.NE, "!="));
    map.put(PseudoProductionEnum.expr_band, new Operator(JCTree.AND, "&&"));
    map.put(PseudoProductionEnum.expr_bor, new Operator(JCTree.OR, "||"));
    
    map.put(PseudoProductionEnum.expr_gt, new Operator(JCTree.GT, ">"));
    map.put(PseudoProductionEnum.expr_ge, new Operator(JCTree.GE, ">="));
    map.put(PseudoProductionEnum.expr_lt, new Operator(JCTree.LT, "<"));
    map.put(PseudoProductionEnum.expr_le, new Operator(JCTree.LE, "<="));
    
    map.put(PseudoProductionEnum.expr_minus, new Operator(JCTree.MINUS, "-"));
    map.put(PseudoProductionEnum.expr_plus, new Operator(JCTree.PLUS, "+"));
    map.put(PseudoProductionEnum.expr_star, new Operator(JCTree.MUL, "*"));
    map.put(PseudoProductionEnum.expr_slash, new Operator(JCTree.DIV, "/"));
    map.put(PseudoProductionEnum.expr_mod, new Operator(JCTree.MOD, "%"));
    
    exprOperatorMap = map;
  }
  
  private JCExpression staticExpression(Expr expr, Operator operator, JCExpression left, JCExpression right) {
    PseudoProductionEnum kind = expr.getKind();
    TreeMaker treeMaker = maker(expr);
    int opcode = operator.opcode;
    
    // unary
    switch(kind) {
      case expr_neg:
      case expr_unary_minus:
      case expr_unary_plus:
        return treeMaker.Unary(opcode, right);
      default:
    }
    
    // binary
    switch(kind) {
      case expr_eq:
      case expr_ne:
      case expr_band:
      case expr_bor:
    
      case expr_gt:
      case expr_ge:
      case expr_lt:
      case expr_le:
        
      case expr_plus:
      case expr_minus:
      case expr_star:
      case expr_slash:
      case expr_mod:
        return treeMaker.Binary(opcode, left, right);
    //TODO
    //case expr_pow:  
      default:
    }
    throw new AssertionError("Unknown staticExpression "+kind);
  }
  
  @Override
  protected JCTree visit(Expr expr, GenEnv genEnv) {
    java.util.List<Node> nodeList = expr.nodeList();
    int size = nodeList.size();
    
    Node leftNode = nodeList.get(0);
    Type leftType = typeCheck.getTypeMap().get(leftNode);
    boolean isDynamic = leftType == PrimitiveType.ANY;
    JCExpression left = gen(leftNode, JCExpression.class, new GenEnv(leftType));
    Node rightNode = null;
    JCExpression right = null;
    Type rightType = null;
    if (size == 2) {
      rightNode = nodeList.get(nodeList.size() - 1);
      rightType = typeCheck.getTypeMap().get(rightNode);
      isDynamic = isDynamic || rightType == PrimitiveType.ANY;
      right = gen(rightNode, JCExpression.class, new GenEnv(rightType));
    }
    
    PseudoProductionEnum kind = expr.getKind();
    Operator operator = exprOperatorMap.get(kind);
    if (!isDynamic) {
      // special cases: test with strings
      // l <= r is converted to l.compareTo(v) <= 0
      if (leftType == PrimitiveType.STRING &&
          rightType == PrimitiveType.STRING &&
          (kind == PseudoProductionEnum.expr_gt ||
           kind == PseudoProductionEnum.expr_ge ||
           kind == PseudoProductionEnum.expr_lt ||
           kind == PseudoProductionEnum.expr_le ||
           kind == PseudoProductionEnum.expr_eq ||
           kind == PseudoProductionEnum.expr_ne)) {
        left = maker(expr).Apply(List.<JCExpression>nil(),
            maker(expr).Select(left, nameFromString("compareTo")),
            List.of(right));
        right = maker(expr).Literal(TypeTags.INT, 0);
      }
      
      return staticExpression(expr, operator, left, right);
    }
    
    Invocation invocation = typeCheck.getInvocationMap().get(expr);
    FunType funType = invocation.getFunType();
    
    // try to adjust the return type to the expected return type
    Type returnType = funType.getReturnType();
    Type expectedReturnType = genEnv.getExpectedReturnType();
    if (returnType == PrimitiveType.ANY && expectedReturnType != PrimitiveType.ANY) {
      returnType = expectedReturnType;
      // fix computed type
      typeCheck.getTypeMap().put(expr, returnType);
    }
    
    // evaluation of && and || is never dynamic
    if (kind == PseudoProductionEnum.expr_band ||
        kind == PseudoProductionEnum.expr_bor) {
      return staticExpression(expr, operator,
          retype(PrimitiveType.BOOLEAN, leftNode, leftType, left),
          retype(PrimitiveType.BOOLEAN, rightNode, rightType, right));
    }
    
    List<JCExpression> exprs = (size == 1)?List.of(left):List.of(left, right);
    JCFieldAccess method = maker(expr).Select(qualifiedIdentifier(expr, "java.dyn.InvokeDynamic"),
        nameFromString("__operator__:"+operator.name));
    return maker(expr).Apply(List.of(asType(expr, returnType)), method, exprs);
  }
  
  //private int letVarCounter = 0;
}