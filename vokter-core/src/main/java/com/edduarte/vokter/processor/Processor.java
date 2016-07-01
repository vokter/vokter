package com.edduarte.vokter.processor;

import java.util.concurrent.Callable;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Processor<Input, Output> {

    Callable<Output> process(Input input);
}
