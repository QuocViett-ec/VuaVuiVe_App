package vn.vuavuive.admin.ui.chatbot;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.FragmentAdminChatBinding;
import vn.vuavuive.shared.data.dto.DashboardStats;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.Shipment;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.util.CurrencyFormatter;

public class AdminChatFragment extends Fragment implements ChatAdapter.OnQuickReplyClickListener {

    private FragmentAdminChatBinding binding;
    private ChatAdapter adapter;
    private MockRepository repo;
    private User currentUser;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repo = MockRepository.getInstance();
        currentUser = repo.getCurrentUser();

        setupRecyclerView();
        setupInputAndSend();
        setupChips();
        
        // Add initial bot greeting
        addBotGreeting();
    }

    private void setupRecyclerView() {
        binding.rvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatAdapter(new ArrayList<>(), this);
        binding.rvChat.setAdapter(adapter);
    }

    private void addBotGreeting() {
        String greeting = "Xin chào " + (currentUser != null ? currentUser.getName() : "Quản trị viên") + "! " +
                "Tôi là Trợ lý AI Vựa Vui Vẻ. Tôi có thể giúp bạn tra cứu nhanh số liệu tồn kho, thống kê đơn hàng, hoặc tìm kiếm trạng thái vận đơn.\n\nHãy nhấn vào các gợi ý bên dưới hoặc gõ trực tiếp câu hỏi của bạn nhé! 👇";
        
        List<String> suggestions = Arrays.asList("Tổng quan hôm nay", "Sắp hết hàng", "Đơn chờ xử lý");
        adapter.addMessage(new ChatAdapter.ChatMessage(greeting, true, getCurrentTimestamp(), suggestions));
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    private void setupInputAndSend() {
        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupChips() {
        binding.chipOverview.setOnClickListener(v -> triggerQuery("Tổng quan hôm nay"));
        binding.chipPending.setOnClickListener(v -> triggerQuery("Đơn chờ xử lý"));
        binding.chipLate.setOnClickListener(v -> triggerQuery("Đơn giao trễ"));
        binding.chipLowStock.setOnClickListener(v -> triggerQuery("Sản phẩm sắp hết hàng"));
        binding.chipCancelRisk.setOnClickListener(v -> triggerQuery("Nguy cơ hủy đơn"));
    }

    private void triggerQuery(String queryText) {
        addUserMessage(queryText);
        processAIResponse(queryText);
    }

    private void sendMessage() {
        String query = binding.etMessage.getText().toString().trim();
        if (query.isEmpty()) return;

        binding.etMessage.setText("");
        addUserMessage(query);
        processAIResponse(query);
    }

    private void addUserMessage(String text) {
        adapter.addMessage(new ChatAdapter.ChatMessage(text, false, getCurrentTimestamp()));
        binding.rvChat.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void processAIResponse(String query) {
        String q = query.toLowerCase().trim();
        
        // Simulate thinking animation or immediate state check
        handler.postDelayed(() -> {
            if (binding == null || adapter == null) return;
            String reply;
            List<String> replies = new ArrayList<>();

            if (q.contains("tổng quan") || q.contains("doanh thu") || q.contains("overview")) {
                DashboardStats stats = repo.getDashboardStats();
                reply = "📊 **BÁO CÁO NHANH DOANH THU & ĐƠN HÀNG**\n\n" +
                        "• Tổng đơn hàng: **" + stats.getTotalOrders() + "** đơn\n" +
                        "• Doanh thu thực tế (đã giao): **" + CurrencyFormatter.formatVnd(stats.getTotalRevenue()) + "**\n" +
                        "• Đơn chờ xử lý: ⏳ **" + stats.getPendingCount() + "** đơn\n" +
                        "• Số lượng thành viên: 👥 **" + stats.getTotalUsers() + "** tài khoản\n\n" +
                        "Dữ liệu được cập nhật thời gian thực từ Mock Database.";
                replies.addAll(Arrays.asList("Đơn chờ xử lý", "Sản phẩm sắp hết hàng"));

            } else if (q.contains("hết hàng") || q.contains("tồn kho") || q.contains("stock")) {
                List<Product> lowStock = new ArrayList<>();
                for (Product p : repo.getProducts()) {
                    if (p.getStock() <= 10) {
                        lowStock.add(p);
                    }
                }

                if (lowStock.isEmpty()) {
                    reply = "✅ Tuyệt vời! Hiện tại không có sản phẩm nào có nguy cơ thiếu hụt tồn kho (tất cả đều > 10 sản phẩm).";
                } else {
                    StringBuilder sb = new StringBuilder("⚠️ **CẢNH BÁO TỒN KHO THẤP (Stock <= 10)**\n\n");
                    for (Product p : lowStock) {
                        String warningSymbol = p.getStock() == 0 ? "❌ [HẾT HÀNG]" : "⚠️ [SẮP HẾT]";
                        sb.append("• ").append(p.getName())
                                .append("\n  ↳ Tồn kho: **").append(p.getStock()).append("** ").append(p.getUnit())
                                .append(" ").append(warningSymbol).append("\n\n");
                    }
                    sb.append("Vui lòng liên hệ nhà vườn để nhập thêm nông sản chất lượng.");
                    reply = sb.toString();
                }
                replies.addAll(Arrays.asList("Tổng quan hôm nay", "Đơn chờ xử lý"));

            } else if (q.contains("chờ xử lý") || q.contains("đơn mới") || q.contains("pending")) {
                List<Order> pendingOrders = new ArrayList<>();
                for (Order o : repo.getOrders()) {
                    if ("pending".equals(o.getStatus())) {
                        pendingOrders.add(o);
                    }
                }

                if (pendingOrders.isEmpty()) {
                    reply = "🎉 Tuyệt vời! Không còn đơn hàng nào ở trạng thái Chờ xác nhận.";
                } else {
                    StringBuilder sb = new StringBuilder("⏳ **DANH SÁCH ĐƠN HÀNG CHỜ XÁC NHẬN (" + pendingOrders.size() + ")**\n\n");
                    for (Order o : pendingOrders) {
                        sb.append("• Mã đơn: **").append(o.getId()).append("**\n")
                                .append("  ↳ Người mua: ").append(o.getDelivery() != null ? o.getDelivery().getName() : "Khách ẩn danh")
                                .append("\n  ↳ Giá trị: **").append(CurrencyFormatter.formatVnd(o.getTotalAmount())).append("**\n\n");
                    }
                    sb.append("Mẹo: Nhập mã đơn hàng (Ví dụ: `ORD-9843A`) vào ô chat để tra cứu vận chuyển chi tiết.");
                    reply = sb.toString();
                    
                    // Add quick replies dynamically for these orders
                    for (Order o : pendingOrders) {
                        replies.add(o.getId());
                    }
                }

            } else if (q.contains("trễ") || q.contains("late") || q.contains("shipment")) {
                List<Shipment> shipments = repo.getShipments();
                List<Shipment> delayed = new ArrayList<>();
                for (Shipment s : shipments) {
                    if ("pending".equals(s.getCurrentStatus()) || "shipping".equals(s.getCurrentStatus())) {
                        delayed.add(s);
                    }
                }

                if (delayed.isEmpty()) {
                    reply = "🚚 Mọi đơn vận chuyển hiện tại đều đã hoàn tất giao hàng thành công.";
                } else {
                    StringBuilder sb = new StringBuilder("🚚 **VẬN ĐƠN ĐANG TRÊN ĐƯỜNG GIAO (" + delayed.size() + ")**\n\n");
                    for (Shipment s : delayed) {
                        sb.append("• Vận đơn: **").append(s.getTrackingNumber()).append("**\n")
                                .append("  ↳ Đơn gốc: ").append(s.getOrderId())
                                .append("\n  ↳ Trạng thái: **").append(s.getCurrentStatus().toUpperCase()).append("**\n\n");
                    }
                    reply = sb.toString();
                }
                replies.add("Tổng quan hôm nay");

            } else if (q.contains("nguy cơ") || q.contains("hủy đơn") || q.contains("risk")) {
                reply = "🚨 **ĐÁNH GIÁ NGUY CƠ HỦY ĐƠN HÀNG**\n\n" +
                        "• Đơn hàng **ORD-9843A** ở trạng thái `COD - CHỜ XÁC NHẬN` hơn 24 giờ. Khách hàng chưa phản hồi điện thoại.\n" +
                        "↳ Mức độ rủi ro: **CAO 🔴**\n\n" +
                        "Khuyến nghị: Nhân viên trực tổng đài gọi điện kiểm tra địa chỉ trước khi xác nhận đơn.";
                replies.add("ORD-9843A");

            } else if (q.startsWith("ord-")) {
                // Specific Order Lookup!
                Order order = null;
                for (Order o : repo.getOrders()) {
                    if (o.getId().equalsIgnoreCase(q)) {
                        order = o;
                        break;
                    }
                }

                if (order != null) {
                    String statusVietnamese = order.getStatus();
                    if ("pending".equals(statusVietnamese)) statusVietnamese = "Đang chờ xác nhận ⏳";
                    else if ("confirmed".equals(statusVietnamese)) statusVietnamese = "Đã xác nhận & Chuẩn bị đóng gói 📦";
                    else if ("delivered".equals(statusVietnamese)) statusVietnamese = "Giao hàng thành công hoàn tất 🎉";
                    
                    String payStatus = order.getPayment() != null ? order.getPayment().getStatus() : "N/A";
                    String gateway = order.getPayment() != null ? order.getPayment().getGateway() : "COD";

                    reply = "🔍 **TRA CỨU CHI TIẾT ĐƠN HÀNG: " + order.getId() + "**\n\n" +
                            "• Khách hàng: **" + (order.getDelivery() != null ? order.getDelivery().getName() : "N/A") + "**\n" +
                            "• Địa chỉ: " + (order.getDelivery() != null ? order.getDelivery().getAddress() : "N/A") + "\n" +
                            "• Giá trị đơn hàng: **" + CurrencyFormatter.formatVnd(order.getTotalAmount()) + "**\n" +
                            "• Thanh toán: " + payStatus.toUpperCase() + " (Qua " + gateway + ")\n" +
                            "• Trạng thái hiện tại: **" + statusVietnamese + "**\n\n" +
                            "Cần hỗ trợ chỉnh sửa đơn hàng? Hãy truy cập tab Đơn Hàng.";
                } else {
                    reply = "❌ Không tìm thấy đơn hàng nào khớp với mã **" + query.toUpperCase() + "**. Vui lòng kiểm tra lại.";
                }
                replies.add("Đơn chờ xử lý");

            } else {
                reply = "💡 Câu hỏi của bạn rất thú vị! Tuy nhiên dưới vai trò trợ lý thống kê nông trại Vựa Vui Vẻ, tôi khuyên bạn nên tập trung hỏi các vấn đề liên quan đến:\n\n" +
                        "1. **Tổng quan** (Doanh thu, số lượng đơn)\n" +
                        "2. **Tồn kho** (Sản phẩm hết hàng, sắp hết)\n" +
                        "3. **Mã đơn** (Ví dụ gõ `ORD-9843A` để tra cứu)\n\n" +
                        "Chúc bạn một ngày quản lý nông trại đầy niềm vui! 🌾";
                replies.addAll(Arrays.asList("Tổng quan hôm nay", "Sản phẩm sắp hết hàng"));
            }

            adapter.addMessage(new ChatAdapter.ChatMessage(reply, true, getCurrentTimestamp(), replies));
            binding.rvChat.scrollToPosition(adapter.getItemCount() - 1);
        }, 800);
    }

    // Callbacks from ChatAdapter options click
    @Override
    public void onQuickReplyClick(String replyText) {
        triggerQuery(replyText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
