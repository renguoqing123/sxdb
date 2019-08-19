package com.sx.db.util;

import com.alibaba.fastjson.JSON;
/**
 * 二叉树
 * @author rengq
 *
 */
public class BinarySearchTree {
	int data;//表示数字
	BinarySearchTree left;
	BinarySearchTree right;
	
	public BinarySearchTree (int data) {
		this.data=data;
		left=null;
		right=null;
	}
	//左小右大
	public void insert(BinarySearchTree root,int data) {
		if(root.data < data) {//根节点小于data，插入右子树
			if(root.right == null) {
				root.right = new BinarySearchTree(data);
			}else {
				insert(root.right,data);
			}
		}else {//根节点大于data，插入左子树
			if(root.left == null) {
				root.left = new BinarySearchTree(data);
			}else {
				insert(root.left, data);
			}
		}
	}
	//中序遍历
	public void in(BinarySearchTree root) {
		if(root!=null) {
			in(root.left);
			System.out.print(root.data + " ");
			in(root.right);
		}
	}
	
	public static void main(String[] args) {
		int data[]= {8,2,3,4,7,1,10};
		BinarySearchTree root = new BinarySearchTree(data[0]);
		for(int i=1;i<data.length;i++) {
			root.insert(root, data[i]);
		}
		System.out.println(JSON.toJSON(root.data));
		root.in(root);
	}
	
}
