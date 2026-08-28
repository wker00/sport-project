import axios from './axios'

// ════════════════════════
// 1. 用户管理
// ════════════════════════

export const userRegister = (data) => axios.post('/user/register', data)

export const userLogin = (data) => axios.post('/user/login', data)

export const getUserInfo = () => axios.get('/user/info')

export const updateUserInfo = (data) => axios.put('/user/updateInfo', data)

export const uploadAvatar = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return axios.post('/user/avatar', fd)
}

export const updatePassword = (data) => axios.put('/user/password', data)

export const userLogout = () => axios.post('/user/logout')

// ════════════════════════
// 2. 商品浏览
// ════════════════════════

export const getProductList = (params = {}) =>
  axios.get('/user/product/list', { params })

export const getProductDetail = (id) => axios.get(`/user/product/${id}`)

export const searchProduct = (keyword, params = {}) =>
  axios.get('/user/product/search', { params: { keyword, ...params } })

export const getHotProducts = () => axios.get('/user/product/hot')

export const getCategoryList = (params = {}) => axios.get('/user/product/categories', { params })

export const getProductReviews = (productId) =>
  axios.get(`/user/product/${productId}/reviews`)

// ════════════════════════
// 3. 购物车
// ════════════════════════

export const getCartList = () => axios.get('/user/cart')

export const addCart = (data) => axios.post('/user/cart', data)

export const updateCart = (id, data) => axios.put(`/user/cart/${id}`, data)

export const deleteCartItem = (id) => axios.delete(`/user/cart/${id}`)

export const clearCart = () => axios.delete('/user/cart')

export const checkCartItem = (id, checked) =>
  axios.put(`/user/cart/${id}/check`, null, { params: { checked } })

// ════════════════════════
// 4. 订单
// ════════════════════════

export const createOrder = (data) => axios.post('/user/order', data)

export const getOrderList = () => axios.get('/user/order')

export const getOrderDetail = (id) => axios.get(`/user/order/${id}`)

export const payOrder = (id) => axios.put(`/user/order/${id}/pay`)

export const cancelOrder = (id) => axios.put(`/user/order/${id}/cancel`)

export const confirmOrder = (id) => axios.put(`/user/order/${id}/confirm`)

export const reviewOrder = (id, data) =>
  axios.post(`/user/order/${id}/review`, data)

export const buyNow = (data) => axios.post('/user/order/buyNow', data)

export const refundOrder = (id, data) =>
  axios.put(`/user/order/${id}/refund`, data)

// ════════════════════════
// 5. 地址
// ════════════════════════

export const getAddressList = () => axios.get('/user/address')

export const getAddressDetail = (id) => axios.get(`/user/address/${id}`)

export const addAddress = (data) => axios.post('/user/address', data)

export const updateAddress = (id, data) =>
  axios.put(`/user/address/${id}`, data)

export const deleteAddress = (id) => axios.delete(`/user/address/${id}`)

export const setDefaultAddress = (id) =>
  axios.put(`/user/address/${id}/default`)

// ════════════════════════
// 6. 优惠券
// ════════════════════════

export const getCouponList = () => axios.get('/user/coupon/list')

export const getMyCoupons = () => axios.get('/user/coupon/my')

export const claimCoupon = (data) => axios.post('/user/coupon/claim', data)

// ════════════════════════
// 7. 积分
// ════════════════════════

export const getPointsBalance = () => axios.get('/user/points/balance')

export const getPointsRecords = () => axios.get('/user/points/records')

export const getPointsGifts = () => axios.get('/user/points/gifts')

export const exchangeGift = (data) =>
  axios.post('/user/points/exchange', data)

export const getExchangeOrders = () =>
  axios.get('/user/points/exchange/orders')

export const getSigninStatus = () => axios.get('/user/points/signin/status')

export const signin = () => axios.post('/user/points/signin')

export const confirmExchangeOrder = (id) =>
  axios.put(`/user/points/exchange/${id}/confirm`)


