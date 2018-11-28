package solver.api.model;

/**
 * Created by canaud on 9/29/2015.
 */
public interface Model {

    enum Comparison {

        LESS_EQUAL("<="),
        EQUAL("="),
        GREATER_EQUAL(">=");

        private String sign;

        Comparison(String sign) {
            this.sign = sign;
        }


        public String getSign() {
            return sign;
        }
    }
}
