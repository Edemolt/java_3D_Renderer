package org.kushagra;

public class Matrix {
    double [] values;

    public Matrix(double[] values) {
        this.values = values;
    }

    Matrix multiply(Matrix obj){
        double [] res = new double[9];

        for(int row = 0; row < 3; row++){
            for(int col = 0; col < 3; col++){
                for(int i = 0; i < 3; i++) res[row * 3 + col] += this.values[row * 3 + i] * obj.values[i * 3 + col];
            }
        }
        return new Matrix(res);
    }

    Vertex transform(Vertex in) {
        return new Vertex(
                in.x * values[0] + in.y * values[3] + in.z * values[6],
                in.x * values[1] + in.y * values[4] + in.z * values[7],
                in.x * values[2] + in.y * values[5] + in.z * values[8]
        );
    }
}
