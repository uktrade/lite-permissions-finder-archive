package controllers.prototype;

import java.util.Iterator;

class PrototypeTreeNodeIterator<T> implements Iterator<PrototypeTreeNode<T>> {

  enum ProcessStages {
    ProcessParent, ProcessChildCurNode, ProcessChildSubNode
  }

  private PrototypeTreeNode<T> treeNode;
  private ProcessStages doNext;
  private PrototypeTreeNode<T> next;
  private Iterator<PrototypeTreeNode<T>> childrenCurNodeIter;
  private Iterator<PrototypeTreeNode<T>> childrenSubNodeIter;

  public PrototypeTreeNodeIterator(PrototypeTreeNode<T> treeNode) {
    this.treeNode = treeNode;
    this.doNext = ProcessStages.ProcessParent;
    this.childrenCurNodeIter = treeNode.children.iterator();
  }

  @Override
  public boolean hasNext() {

    if (this.doNext == ProcessStages.ProcessParent) {
      this.next = this.treeNode;
      this.doNext = ProcessStages.ProcessChildCurNode;
      return true;
    }

    if (this.doNext == ProcessStages.ProcessChildCurNode) {
      if (childrenCurNodeIter.hasNext()) {
        PrototypeTreeNode<T> childDirect = childrenCurNodeIter.next();
        childrenSubNodeIter = childDirect.iterator();
        this.doNext = ProcessStages.ProcessChildSubNode;
        return hasNext();
      }

      else {
        this.doNext = null;
        return false;
      }
    }

    if (this.doNext == ProcessStages.ProcessChildSubNode) {
      if (childrenSubNodeIter.hasNext()) {
        this.next = childrenSubNodeIter.next();
        return true;
      }
      else {
        this.next = null;
        this.doNext = ProcessStages.ProcessChildCurNode;
        return hasNext();
      }
    }

    return false;
  }

  @Override
  public PrototypeTreeNode<T> next() {
    return this.next;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
