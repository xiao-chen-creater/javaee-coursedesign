/**
 * @template T
 * @param success {boolean}
 * @param message {string}
 * @param data {T}
 */
class JsonResult {
}

/**
 * GET 请求
 *
 * @template T
 * @param url {string} api 路径
 * @param data {any | undefined}
 * @returns {Promise<JsonResult<T>>}
 */
async function get(url, data = undefined) {
    const postfix = data ? '?' + new URLSearchParams(data).toString() : '';
    const response = await fetch(window.APP_CONTEXT_PATH + url + postfix);
    /**
     * @template T
     * @type {JsonResult<T>}
     */
    const result = await response.json();

    if (result.success) {
        return result;
    }

    throw new Error(result.message);
}

/**
 * POST 请求
 *
 * @template T
 * @param url {string} api 路径
 * @param data {any | undefined}
 * @returns {Promise<JsonResult<T>>}
 */
async function post(url, data) {
    const response = await fetch(window.APP_CONTEXT_PATH + url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: data ? JSON.stringify(data) : null
    });
    /**
     * @template T
     * @type {JsonResult<T>}
     */
    const result = await response.json();

    if (result.success) {
        return result;
    }

    throw new Error(result.message);
}

// user

/**
 * 登录
 *
 * @param username {string}
 * @param password {string}
 * @param redirect {string}
 * @returns {Promise<string>}
 */
async function login(username, password, redirect = '/') {
    const result = await post(`/user/login`, {username, password, redirect});
    return result.data;
}

/**
 * 忘记密码
 * （直接修改密码，仅用于测试）
 *
 * @param username {string}
 * @param password {string}
 * @param redirect {string}
 * @returns {Promise<string>}
 */
async function forgetPassword(username, password, redirect = '/') {
    const result = await post(`/user/login/debug`, {username, password, redirect});
    return result.data;
}

/**
 * 注册
 *
 * @param username {string}
 * @param password {string}
 * @param confirmPassword {string}
 * @param email {string}
 * @param redirect {string}
 * @returns {Promise<string>}
 */
async function register(username, password, confirmPassword, email, redirect = '/') {
    if (password.length < 6) {
        throw new Error('密码长度不能少于6位');
    }
    if (password !== confirmPassword) {
        throw new Error("两次密码不一致");
    }
    if (!email.includes('@')) {
        throw new Error('请输入正确的邮箱格式')
    }

    const result = await post(`/user/register`, {username, password, email, redirect});
    return result.data;
}

/**
 * 修改用户信息
 *
 * @param email {string | undefined}
 * @param phone {string | undefined}
 * @param address {string | undefined}
 * @returns {Promise<void>}
 */
async function updateProfile(email, phone, address) {
    if (email && !email.includes('@')) {
        throw new Error('请输入正确的邮箱格式')
    }

    if (phone && !/^1[3-9]\d{9}$/.test(phone)) {
        throw new Error('请输入正确的手机格式')
    }

    await post('/user/profile', {email, phone, address});
}

async function updatePassword(oldPassword, newPassword, confirmPassword) {
    if (!oldPassword || !newPassword || !confirmPassword) {
        throw new Error('请填写完整信息');
    }

    if (newPassword !== confirmPassword) {
        throw new Error('两次密码输入不一致');
    }

    if (newPassword.length < 6) {
        throw new Error('密码长度不能少于6位');
    }

    await post('/user/password', {oldPassword, newPassword, confirmPassword});
}

// cart

/**
 * 获取购物车数量
 *
 * @returns {Promise<number>}
 */
async function getCartCount() {
    const result = await get(`/cart/count`);
    return result.data;
}

/**
 * 添加到购物车
 *
 * @param productId {number}
 * @param quantity {number}
 * @returns {Promise<string>}
 */
async function addToCart(productId, quantity = 1) {
    const result = await get(`/cart/add`, {productId, quantity});
    return result.message;
}

/**
 * 修改购物车某商品数量
 *
 * @param productId
 * @param quantity
 * @returns {Promise<string>}
 */
async function updateCartProductCount(productId, quantity) {
    const result = await get('/cart/update', {productId, quantity});
    return result.message;
}

/**
 * 删除购物车中某个物品
 *
 * @param productId {number}
 * @returns {Promise<void>}
 */
async function removeCartItem(productId) {
    await get('/cart/remove', {productId});
}

/**
 * 批量删除购物车物品
 * @param productIds
 * @returns {Promise<int[]>}
 */
async function removeCartItems(productIds) {
    if (!productIds || productIds.length === 0) {
        throw new Error('请选择要删除的商品');
    }

    const result = await post('/cart/remove', productIds)
    return result.data;
}

// order

/**
 * 创建订单
 * @param productIds {number[] | string[]}
 * @returns {Promise<Order>}
 */
async function createOrder(productIds) {
    const result = await post('/order/create', productIds);
    return result.data;
}

/**
 * 更新订单状态
 * @param id {number}
 * @param status { 'pending' | 'paid' | 'shipped' | 'completed' | 'cancelled' }
 * @returns {Promise<JsonResult<void>>}
 */
async function updateStatus(id, status) {
    return await post('/order/status', {id, status});
}

// search

/**
 * 搜索建议
 *
 * @param keyword {string}
 * @param limit {number} 建议数量
 * @returns {Promise<string[]>}
 */
async function getSuggestions(keyword, limit) {
    if (keyword.length === 0) throw Error("请输入搜索关键词");
    const result = await get("/search/suggestions", {keyword, limit});
    return result.data;
}