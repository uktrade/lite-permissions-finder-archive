package controllers.prototype;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PrototypeTreeNode<T> implements Iterable<PrototypeTreeNode<T>>{
  T data;
  PrototypeTreeNode<T> parent;
  List<PrototypeTreeNode<T>> children;

  public PrototypeTreeNode(T data) {
    this.data = data;
    this.children = new LinkedList<>();
  }

  public PrototypeTreeNode() {
  }

  public PrototypeTreeNode<T> addChild(T child) {
    PrototypeTreeNode<T> childNode = new PrototypeTreeNode<>(child);
    childNode.parent = this;
    this.children.add(childNode);
    return childNode;
  }

  public boolean isRoot() {
    return parent == null;
  }

  public boolean isLeaf() {
    return children.size() == 0;
  }

  @Override
  public Iterator<PrototypeTreeNode<T>> iterator() {
    PrototypeTreeNodeIterator<T> iter = new PrototypeTreeNodeIterator<T>(this);
    return iter;
  }

  public T getData() {
    return data;
  }

  public PrototypeTreeNode<T> getParent() {
    return parent;
  }

  public List<PrototypeTreeNode<T>> getChildren() {
    return children;
  }

  public int getLevel() {
    if (this.isRoot())
      return 0;
    else
      return parent.getLevel() + 1;
  }
}
