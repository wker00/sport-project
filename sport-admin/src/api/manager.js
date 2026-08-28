import axios from './axios'

// ===== 1. 管理员认证 /api/admin =====

export const adminLogin = (data) => {
    return axios.post('/admin/login', data) //登录接口
}

export const adminLogout = () => {
    return axios.post('/admin/logout') //退出登录接口
}

export const adminRegister = (data) => {
    return axios.post('/admin/register', data) //创建管理员接口
}

export const getAdminInfo = () => {
    return axios.get('/admin/info') //获取管理员信息接口
}

export const updateAdminInfo = (data) => {
    return axios.put('/admin/info', data) //更新管理员信息接口
}

export const updateAdminInfoById = (id, data) => {
    return axios.put(`/admin/${id}/info`, data) //超级管理员更新其他管理员信息接口
}

export const getAdminList = () => {
    return axios.get('/admin/list') //获取管理员列表接口
}

export const uploadAdminAvatar = (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return axios.post('/admin/avatar', formData) //上传管理员头像接口
}

export const updateAdminPassword = (data) => {
    return axios.put('/admin/password', data) //修改密码接口
}

export const deleteAdmin = (id) => {
    return axios.delete(`/admin/${id}`) //删除管理员接口
}

export const enableAdmin = (id) => {
    return axios.put(`/admin/${id}/enable`) //启用管理员接口
}

export const disableAdmin = (id) => {
    return axios.put(`/admin/${id}/disable`) //禁用管理员接口
}

export const resetAdminPassword = (id) => {
    return axios.put(`/admin/${id}/password`) //超级管理员重置其他管理员密码接口
}

// ===== 2. 分类管理 /api/admin/category =====

export const getCategoryList = () => {
    return axios.get('/admin/category/list') //获取分类列表接口
}

export const getCategoryDetail = (id) => {
    return axios.get(`/admin/category/${id}`) //获取分类详情接口
}

export const createCategory = (data) => {
    return axios.post('/admin/category', data, {
        headers: { 'Content-Type': undefined },
    }) //创建分类接口
}

export const updateCategory = (id, data) => {
    return axios.put(`/admin/category/${id}`, data, {
        headers: { 'Content-Type': undefined },
    }) //更新分类接口
}

export const deleteCategory = (id) => {
    return axios.delete(`/admin/category/${id}`) //删除分类接口
}

// ===== 3. 优惠券管理 /api/admin/coupon =====

export const getCouponList = () => {
    return axios.get('/admin/coupon/list') //获取优惠券模板列表
}

export const getCouponDetail = (id) => {
    return axios.get(`/admin/coupon/${id}`) //获取优惠券模板详情
}

export const createCoupon = (data) => {
    return axios.post('/admin/coupon', data) //创建优惠券
}

export const updateCoupon = (id, data) => {
    return axios.put(`/admin/coupon/${id}`, data) //更新优惠券
}

export const deleteCoupon = (id) => {
    return axios.delete(`/admin/coupon/${id}`) //删除优惠券
}

export const getCouponRecords = (id) => {
    return axios.get(`/admin/coupon/${id}/records`) //获取优惠券发放记录
}

// ===== 4. 积分商品管理 /api/admin/points/gifts =====

export const getPointsGiftsList = () => {
    return axios.get('/admin/points/gifts/list') //积分商品列表
}

export const getPointsGiftDetail = (id) => {
    return axios.get(`/admin/points/gifts/${id}`)//积分商品详情
}

export const createPointsGift = (data) => {
    return axios.post('/admin/points/gifts', data, {
        headers: { 'Content-Type': undefined },
    })
}

export const updatePointsGift = (id, data) => {
    return axios.put(`/admin/points/gifts/${id}`, data, {
        headers: { 'Content-Type': undefined },
    })
}

export const deletePointsGift = (id) => {
    return axios.delete(`/admin/points/gifts/${id}`)
}

export const getExchangeOrderList = () => {
    return axios.get('/admin/points/gifts/orders')
}

export const shipExchangeOrder = (id, data) => {
    return axios.put(`/admin/points/gifts/orders/${id}/ship`, data)
}

// ===== 5. 商品管理 /api/admin/product =====

export const uploadProductImage = (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return axios.post('/admin/product/upload', formData) //上传商品图片接口
}

export const createProduct = (data) => {
    return axios.post('/admin/product', data) //创建商品接口
}

export const updateProduct = (id, data) => {
    return axios.put(`/admin/product/${id}`, data) //更新商品接口
}

export const deleteProduct = (id) => {
    return axios.delete(`/admin/product/${id}`) //删除商品接口
}

export const getProductDetail = (id) => {
    return axios.get(`/admin/product/${id}`) //获取商品详情接口
}

export const getProductList = (params = {}) => {
    return axios.get('/admin/product/list', { params }) //获取商品列表接口（分页）
}

export const getProductByCategory = (categoryId) => {
    return axios.get(`/admin/product/category/${categoryId}`) //获取商品分类接口
}

export const searchProduct = (keyword, categoryId) => {
    const params = { keyword }
    if (categoryId != null) params.categoryId = categoryId
    return axios.get('/admin/product/search', { params }) //搜索商品接口
}

// ===== 6. 订单管理 /api/admin/order =====

export const getOrderList = (status) => {
    return axios.get('/admin/order/list', { params: { status } }) //获取订单列表接口
}

export const getOrderDetail = (id) => {
    return axios.get(`/admin/order/${id}`) //获取订单详情接口
}

export const shipOrder = (data) => {
    return axios.put('/admin/order/ship', data) //发货接口
}

export const refundOrder = (data) => {
    return axios.put('/admin/order/refund', data) //处理退款接口
}

export const deliverOrder = (id) => {
    return axios.put(`/admin/order/${id}/deliver`) //确认送达接口
}

// ===== 7. 用户管理 /api/admin/user =====

export const getUserList = () => {
    return axios.get('/admin/user/list') //获取用户列表接口
}

export const getUserDetail = (id) => {
    return axios.get(`/admin/user/${id}`) //获取用户详情接口
}

export const resetUserPassword = (id) => {
    return axios.put(`/admin/user/${id}/password`) //重置用户密码接口
}

// ===== 8. 仪表盘 /api/admin/dashboard =====

export const getDashboardData = () => {
    return axios.get('/admin/dashboard') //获取仪表盘数据接口
}

export const getDailyStatistics = (startDate, endDate) => {
    return axios.get('/admin/statistics/daily', { params: { startDate, endDate } }) //获取每日统计数据接口
}

// ===== 9. 评价管理 /api/admin/review =====

export const getReviewList = (params = {}) => {
    return axios.get('/admin/review/list', { params }) //获取评价列表接口
}

export const getReviewDetail = (id) => {
    return axios.get(`/admin/review/${id}`) //获取评价详情接口
}

export const replyReview = (id, data) => {
    return axios.put(`/admin/review/${id}/reply`, data) //回复评价接口
}

export const deleteReview = (id) => {
    return axios.delete(`/admin/review/${id}`) //删除评价接口
}

// ===== 10. 操作日志管理 /api/admin/operate-log =====

export const getOperateLogList = (params = {}) => {
    return axios.get('/admin/operate-log/list', { params })
}

export const getOperateLogDetail = (id) => {
    return axios.get(`/admin/operate-log/${id}`)
}

export const deleteOperateLog = (id) => {
    return axios.delete(`/admin/operate-log/${id}`)
}

export const cleanOperateLog = (beforeDate) => {
    return axios.delete('/admin/operate-log/clean', { params: { beforeDate } })
}


