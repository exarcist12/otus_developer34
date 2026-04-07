let stompClient = null;
let currentRoomId = null;

const chatLineElementId = "chatLine";
const roomIdElementId = "roomId";
const messageElementId = "message";

// Новая функция: очистка таблицы сообщений
const clearChatTable = () => {
    const chatLine = document.getElementById(chatLineElementId);
    while (chatLine.rows.length > 0) {
        chatLine.deleteRow(0);
    }
    console.log("Chat table cleared");
}

const setConnected = (connected) => {
    const connectBtn = document.getElementById("connect");
    const disconnectBtn = document.getElementById("disconnect");

    connectBtn.disabled = connected;
    disconnectBtn.disabled = !connected;
    const chatLine = document.getElementById(chatLineElementId);
    chatLine.hidden = !connected;
}

const connect = () => {
    clearChatTable();

    const selectedRoomId = document.getElementById(roomIdElementId).value;
    currentRoomId = selectedRoomId;


    stompClient = Stomp.over(new SockJS('/gs-guide-websocket'));
    stompClient.connect({}, (frame) => {
        setConnected(true);
        const userName = frame.headers["user-name"];
        const roomId = document.getElementById(roomIdElementId).value;
        console.log(`Connected to roomId: ${selectedRoomId} frame:${frame}`);
        const topicName = `/topic/response.${selectedRoomId}`;
        const topicNameUser = `/user/${userName}${topicName}`;
        stompClient.subscribe("/user/queue/errors", (error) => {
            console.error("Error from server:", error.body);
            alert("Error: " + error.body);
        });

        if (roomId === "1408") {
            stompClient.subscribe("/topic/all", (message) => {
                const msg = JSON.parse(message.body).messageStr;
                console.log("Received from /topic/all:", msg);  // для отладки
                showMessage(msg);
            });
            console.log("✅ Subscribed to /topic/all for room 1408");
        }

        stompClient.subscribe(topicName, (message) => showMessage(JSON.parse(message.body).messageStr));
        stompClient.subscribe(topicNameUser, (message) => showMessage(JSON.parse(message.body).messageStr));
    });
}

const disconnect = () => {
    if (stompClient !== null) {
        stompClient.disconnect();
        stompClient = null;
    }

    currentRoomId = null;
    setConnected(false);
    console.log("Disconnected");
}

const sendMsg = () => {

    if (!stompClient || !stompClient.connected) {
        console.log("Not connected");
        return;
    }

    const roomId = document.getElementById(roomIdElementId).value;
    const messageInput = document.getElementById(messageElementId);
    const message = document.getElementById(messageElementId).value;
    if (!message || !message.trim()) {
        console.log("Empty message, not sending");
        return;
    }

    if (roomId === "1408") {
        console.warn("Room 1408 is read-only! Message not sent.");
        alert("Room 1408 is read-only! You can only view messages here.");
        messageInput.value = '';
        return;
    }

    console.log(`Sending message to room ${roomId}: ${message}`);
    stompClient.send(`/app/message.${roomId}`, {}, JSON.stringify({'messageStr': message}));

    messageInput.value = '';
    messageInput.focus();
}

const showMessage = (message) => {
    const chatLine = document.getElementById(chatLineElementId);
    let newRow = chatLine.insertRow(-1);
    let newCell = newRow.insertCell(0);
    let newText = document.createTextNode(message);
    newCell.appendChild(newText);
}
