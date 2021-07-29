package com.youu.mysql.protocol.common;

import com.youu.mysql.protocol.pkg.req.LoginRequest;
import io.netty.util.AttributeKey;

public interface ChannelAttributeKey {
    AttributeKey<LoginRequest> LOGIN_REQUEST = AttributeKey.valueOf("login_request");
    AttributeKey<Integer> STORE_INDEX = AttributeKey.valueOf("store_index");
}
