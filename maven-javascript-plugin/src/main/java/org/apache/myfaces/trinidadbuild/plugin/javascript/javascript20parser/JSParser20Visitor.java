/*
* Copyright 2006 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/* Generated By:JJTree: Do not edit this line. .\JSParser20Visitor.java */

package org.apache.myfaces.trinidadbuild.plugin.javascript.javascript20parser;

public interface JSParser20Visitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(ASTProgram node, Object data);
  public Object visit(ASTIdentifier node, Object data);
  public Object visit(ASTSimpleQualifiedIdentifier node, Object data);
  public Object visit(ASTExpressionQualifiedIdentifier node, Object data);
  public Object visit(ASTQualifiedIdentifier node, Object data);
  public Object visit(ASTPrimaryExpression node, Object data);
  public Object visit(ASTReservedNamespace node, Object data);
  public Object visit(ASTFunctionExpression node, Object data);
  public Object visit(ASTObjectLiteral node, Object data);
  public Object visit(ASTFieldList node, Object data);
  public Object visit(ASTLiteralField node, Object data);
  public Object visit(ASTFieldName node, Object data);
  public Object visit(ASTArrayLiteral node, Object data);
  public Object visit(ASTElementList node, Object data);
  public Object visit(ASTLiteralElement node, Object data);
  public Object visit(ASTSuperExpression node, Object data);
  public Object visit(ASTPostfixExpression node, Object data);
  public Object visit(ASTAttributeExpression node, Object data);
  public Object visit(ASTFullPostfixExpression node, Object data);
  public Object visit(ASTFullNewExpression node, Object data);
  public Object visit(ASTFunctionConstructor node, Object data);
  public Object visit(ASTFullNewSubexpression node, Object data);
  public Object visit(ASTPostfixOp node, Object data);
  public Object visit(ASTPropertyOrArguments node, Object data);
  public Object visit(ASTPropertyOperator node, Object data);
  public Object visit(ASTArguments node, Object data);
  public Object visit(ASTUnaryExpression node, Object data);
  public Object visit(ASTMulOp node, Object data);
  public Object visit(ASTMultiplicativeExpression node, Object data);
  public Object visit(ASTAddOp node, Object data);
  public Object visit(ASTAdditiveExpression node, Object data);
  public Object visit(ASTShiftOp node, Object data);
  public Object visit(ASTShiftExpression node, Object data);
  public Object visit(ASTRelOp node, Object data);
  public Object visit(ASTRelationalExpression node, Object data);
  public Object visit(ASTRelationalExpressionNoIN node, Object data);
  public Object visit(ASTEqualOp node, Object data);
  public Object visit(ASTEqualityExpression node, Object data);
  public Object visit(ASTEqualityExpressionNoIN node, Object data);
  public Object visit(ASTBitwiseANDOp node, Object data);
  public Object visit(ASTBitwiseANDExpression node, Object data);
  public Object visit(ASTBitwiseANDExpressionNoIN node, Object data);
  public Object visit(ASTBitwiseXOROp node, Object data);
  public Object visit(ASTBitwiseXORExpression node, Object data);
  public Object visit(ASTBitwiseXORExpressionNoIN node, Object data);
  public Object visit(ASTBitwiseOROp node, Object data);
  public Object visit(ASTBitwiseORExpression node, Object data);
  public Object visit(ASTBitwiseORExpressionNoIN node, Object data);
  public Object visit(ASTLogicalANDExpression node, Object data);
  public Object visit(ASTLogicalANDExpressionNoIN node, Object data);
  public Object visit(ASTLogicalORExpression node, Object data);
  public Object visit(ASTLogicalORExpressionNoIN node, Object data);
  public Object visit(ASTConditionalExpression node, Object data);
  public Object visit(ASTConditionalExpressionNoIN node, Object data);
  public Object visit(ASTNonAssignmentExpression node, Object data);
  public Object visit(ASTNonAssignmentExpressionNoIN node, Object data);
  public Object visit(ASTAssignementOperator node, Object data);
  public Object visit(ASTAssignmentExpression node, Object data);
  public Object visit(ASTAssignmentExpressionNoIN node, Object data);
  public Object visit(ASTListExpression node, Object data);
  public Object visit(ASTListExpressionNoIN node, Object data);
  public Object visit(ASTTypeExpression node, Object data);
  public Object visit(ASTTypeExpressionNoIN node, Object data);
  public Object visit(ASTTypeExpressionList node, Object data);
  public Object visit(ASTStatement node, Object data);
  public Object visit(ASTSubstatement node, Object data);
  public Object visit(ASTSubstatements node, Object data);
  public Object visit(ASTSc node, Object data);
  public Object visit(ASTEolCommentSkipWs node, Object data);
  public Object visit(ASTEmptyStatement node, Object data);
  public Object visit(ASTExpressionStatement node, Object data);
  public Object visit(ASTSuperStatement node, Object data);
  public Object visit(ASTBlock node, Object data);
  public Object visit(ASTLabeledStatement node, Object data);
  public Object visit(ASTIfStatement node, Object data);
  public Object visit(ASTSwitchStatement node, Object data);
  public Object visit(ASTCaseElements node, Object data);
  public Object visit(ASTCaseElement node, Object data);
  public Object visit(ASTCaseLabel node, Object data);
  public Object visit(ASTDoStatement node, Object data);
  public Object visit(ASTWhileStatement node, Object data);
  public Object visit(ASTForStatement node, Object data);
  public Object visit(ASTForInitializer node, Object data);
  public Object visit(ASTForInBinding node, Object data);
  public Object visit(ASTWithStatement node, Object data);
  public Object visit(ASTContinueStatement node, Object data);
  public Object visit(ASTBreakStatement node, Object data);
  public Object visit(ASTReturnStatement node, Object data);
  public Object visit(ASTThrowStatement node, Object data);
  public Object visit(ASTTryStatement node, Object data);
  public Object visit(ASTDirectives node, Object data);
  public Object visit(ASTDirective node, Object data);
  public Object visit(ASTAnnotatableDirective node, Object data);
  public Object visit(ASTAttributes node, Object data);
  public Object visit(ASTAttribute node, Object data);
  public Object visit(ASTUseDirective node, Object data);
  public Object visit(ASTImportDirective node, Object data);
  public Object visit(ASTIncludeDirective node, Object data);
  public Object visit(ASTPragma node, Object data);
  public Object visit(ASTPragmaItems node, Object data);
  public Object visit(ASTPragmaItem node, Object data);
  public Object visit(ASTPragmaExpr node, Object data);
  public Object visit(ASTPragmaArgument node, Object data);
  public Object visit(ASTExportDefinition node, Object data);
  public Object visit(ASTExportBindingList node, Object data);
  public Object visit(ASTExportBinding node, Object data);
  public Object visit(ASTVariableDefinition node, Object data);
  public Object visit(ASTVariableDefinitionNoIN node, Object data);
  public Object visit(ASTVariableDefinitionKind node, Object data);
  public Object visit(ASTVariableBindingList node, Object data);
  public Object visit(ASTVariableBindingListNoIN node, Object data);
  public Object visit(ASTVariableBinding node, Object data);
  public Object visit(ASTVariableBindingNoIN node, Object data);
  public Object visit(ASTVariableInitialisation node, Object data);
  public Object visit(ASTVariableInitialisationNoIN node, Object data);
  public Object visit(ASTVariableInitializer node, Object data);
  public Object visit(ASTVariableInitializerNoIN node, Object data);
  public Object visit(ASTTypedIdentifier node, Object data);
  public Object visit(ASTTypedIdentifierNoIN node, Object data);
  public Object visit(ASTSimpleVariableDefinition node, Object data);
  public Object visit(ASTUntypedVariableBindingList node, Object data);
  public Object visit(ASTUntypedVariableBinding node, Object data);
  public Object visit(ASTFunctionDefinition node, Object data);
  public Object visit(ASTFunctionName node, Object data);
  public Object visit(ASTFunctionCommon node, Object data);
  public Object visit(ASTParameters node, Object data);
  public Object visit(ASTParameter node, Object data);
  public Object visit(ASTParameterInit node, Object data);
  public Object visit(ASTRestParameters node, Object data);
  public Object visit(ASTResult node, Object data);
  public Object visit(ASTClassDefinition node, Object data);
  public Object visit(ASTInterfaceDefinition node, Object data);
  public Object visit(ASTInheritance node, Object data);
  public Object visit(ASTNamespaceDefinition node, Object data);
  public Object visit(ASTPackageDefinition node, Object data);
  public Object visit(ASTPackageName node, Object data);
  public Object visit(ASTPackageIdentifiers node, Object data);
}