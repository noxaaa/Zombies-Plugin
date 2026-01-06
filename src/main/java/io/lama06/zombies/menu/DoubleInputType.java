package io.lama06.zombies.menu;

public final class DoubleInputType implements InputType<Double> {
    private final double from;
    private final double to;

    public DoubleInputType(final Double from, final Double to) {
        this.from = from == null ? Double.MIN_VALUE : from;
        this.to = to == null ? Double.MAX_VALUE : to;
    }

    public DoubleInputType() {
        this(0.0, Double.MAX_VALUE);
    }

    @Override
    public Double parseInput(final String input) throws InvalidInputException {
        final double inputDouble;
        try {
            inputDouble = Double.parseDouble(input.trim());
        } catch (final NumberFormatException e) {
            throw new InvalidInputException("double incorrectly formatted", e);
        }
        if (inputDouble < 0 && from >= 0) {
            throw new InvalidInputException("negative values not allowed");
        }
        if (inputDouble < from || inputDouble > to) {
            throw new InvalidInputException("value out of range [%s, %s]".formatted(from, to));
        }
        return inputDouble;
    }

    @Override
    public String formatData(final Double data) {
        return data.toString();
    }
}
