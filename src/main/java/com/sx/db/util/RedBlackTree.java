package com.sx.db.util;
/**
 * 红黑树
 * @author rengq
 *
 */
public class RedBlackTree {
	int data;//数据
	RedBlackTree left;
	RedBlackTree right;
	RedBlackTree parent;
	boolean color;
	
	public RedBlackTree(){}
	
	public RedBlackTree(int data,RedBlackTree left,RedBlackTree right,RedBlackTree parent,boolean color) {
		this.data=data;
		this.left=left;
		this.right=right;
		this.parent=parent;
		this.color=color;
	}
	
	//左旋，右旋，插入，修改，删除
	/**
	 * 对某一node节点进行左旋
	 * @param root
	 * @param left
	 * @return
	 */
	public RedBlackTree leftRotate(RedBlackTree root, RedBlackTree x){
		RedBlackTree y = x.right;
		x.right=y.left;
		y.left.parent=x;
		if(x.parent == null) {
			root = y;
		}else {
			if(x.parent.left == x) {
				x.parent.left=y;
			}else {
				x.parent.right=y;
			}
		}
		y.parent=x.parent;
		y.left=x;
		x.parent=y;
		return root;
	}
	
	/**
	 * 对某一node节点进行右旋
	 * @param root
	 * @param x
	 * @return
	 */
	public RedBlackTree rightRotate(RedBlackTree root, RedBlackTree x){
		RedBlackTree y = x.left;
		x.left=y.right;
		y.right.parent=x;
		if(x.parent == null) {
			root = y;
		}else {
			if(x.parent.left == x) {
				x.parent.left=y;
			}else {
				x.parent.right=y;
			}
		}
		y.parent=x.parent;
		y.right=x;
		x.parent=y;
		return root;
	}
}
