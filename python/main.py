import sys
from Adafruit_IO import MQTTClient
import datetime
import tkinter as tk




class ChatApplication(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Monitor")
        
        self.message_frame = tk.Frame(self)
        self.message_frame.pack(padx=10, pady=10)

        self.message_label = tk.Label(self.message_frame, text="Tin nhắn nhận được:")
        self.message_label.grid(row=0, column=0, padx=5, pady=5, sticky="w")

        self.message_display = tk.Text(self.message_frame, width=80, height=15, bg="black")
        self.message_display.grid(row=1, column=0, padx=5, pady=5)

        self.input_frame = tk.Frame(self)
        self.input_frame.pack(padx=10, pady=10)

        self.input_label = tk.Label(self.input_frame, text="Nhập tin nhắn:")
        self.input_label.grid(row=0, column=0, padx=5, pady=5, sticky="w")

        self.selected_feed = tk.StringVar(self.input_frame)
        self.selected_feed.set(AIO_FEED_ID[0])  # Thiết lập giá trị mặc định
        self.feed_select = tk.OptionMenu(self.input_frame, self.selected_feed, *AIO_FEED_ID)
        self.feed_select.grid(row=1, column=0, padx=5, pady=5, sticky="w")

        self.input_entry = tk.Entry(self.input_frame, width=50)
        self.input_entry.grid(row=2, column=0, padx=5, pady=5)

        self.send_button = tk.Button(self.input_frame, text="Gửi", command=self.send_message)
        self.send_button.grid(row=2, column=1, padx=5, pady=5)

        self.message_display.tag_configure("normal", foreground="black")
        self.message_display.tag_configure("error", foreground="red")
        self.message_display.tag_configure("success", foreground="green")
        self.message_display.tag_configure("receive", foreground="yellow")

        self.message_to_send = ""

        self.client = MQTTClient(AIO_USERNAME, AIO_KEY)
        self.client.on_connect = self.connected
        self.client.on_disconnect = self.disconnected
        self.client.on_message = self.message
        self.client.on_subscribe = self.subscribe
        self.client.connect()
        self.client.loop_background()

    def connected(self, client):
        self.message_display.insert(tk.END, f"{self.get_datetime()} Kết nối thành công ...\n", "success")
        self.scroll_to_bottom()
        for topic in AIO_FEED_ID:
            client.subscribe(topic)

    def subscribe(self, client, userdata, mid, granted_qos):
        self.message_display.insert(tk.END, f"{self.get_datetime()} Subscribe thành công {mid} ...\n", "success")
        self.scroll_to_bottom()
    def disconnected(self, client):
        self.message_display.insert(tk.END, f"Ngắt kết nối ...\n")
        self.scroll_to_bottom()
        sys.exit(1)

    def message(self, client, feed_id, payload):
        self.log_message(feed_id, payload)

    def scroll_to_bottom(self):
        self.message_display.see(tk.END)

    def log_message(self, topic, message):
        self.message_display.insert(tk.END, f"{self.get_datetime()} Tin nhắn từ {topic}: {message}\n", "receive")
        self.scroll_to_bottom()  # Cuộn màn hình về vị trí cuối cùng
    def get_datetime(self):
        now = datetime.datetime.now()
        formatted_time = now.strftime("%d/%m/%Y %I:%M:%S %p")
        return formatted_time
    def send_message(self):
        message = self.input_entry.get()
        topic = self.selected_feed.get()
        # Sử dụng topic để gửi tin nhắn tới máy chủ MQTT
        result = self.validate_message(topic, message)
        if result: 
            self.client.publish(topic, message)
            self.message_display.insert(tk.END, self.message_to_send, "success")
        else:
            self.message_display.insert(tk.END, self.message_to_send, "error")
        self.scroll_to_bottom()
    def validate_message(self, topic, message):
        isValid = True
        if message == "":
            isValid = False
            self.message_to_send = f"{self.get_datetime()} Không có giá trị gửi đi\n"
            return isValid
        value = float(message)
        if topic == "s-temperature":
            if value < -273:
                self.message_to_send = f"{self.get_datetime()} Nhiệt độ không được nhỏ hơn -273\n"
                isValid = False
        elif topic == "s-humid":
            if value > 100 or value < 0:
                self.message_to_send = f"{self.get_datetime()} Độ ẩm phải từ 0% - 100%\n"
                isValid = False
        elif topic == "s-lumi":
            if value < 0:
                self.message_to_send = f"{self.get_datetime()} Độ sáng phải lớn hơn 0\n"
                isValid = False
        elif topic == "btn-fan" or topic == "btn-light" or topic == "btn-tv":
            if value != 0 and value != 1:
                self.message_to_send = f"{self.get_datetime()} Trạng thái bật/tắt có giá trị 1/0\n"
                isValid = False
        if isValid:
            self.message_to_send = f"{self.get_datetime()} Đã gửi đến {topic}: {message}\n"
        return isValid
if __name__ == "__main__":
    app = ChatApplication()
    app.mainloop()