package dml.condition;

public class Pair<K,D> {
    K left;
    D right;


    Pair() {}
    Pair(K left, D right) {
        this.left = left;
        this.right = right;
    }

    public void setLeft(K left) {
        this.left = left;
    }

    public void setRight(D right) {
        this.right = right;
    }

    public K getLeft() {
        return left;
    }

    public D getRight() {
        return right;
    }
}
