package ru.dakon.CConventerBot.parser;

import okhttp3.Request;
import okhttp3.Response;

public interface Parser {
    Double parse(String priceAsString);
}
