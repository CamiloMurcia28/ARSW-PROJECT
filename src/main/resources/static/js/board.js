const boardApp = (function () {

    const ROWS = 10;
    const COLS = 15;

    function moveTank(direction) {
        // if (!userTank || isMoving)
        //     return; // Si no hay tanque del usuario o está en movimiento, no hacer nada

        // isMoving = true; // Activar el bloqueo de movimiento al inicio

        let x = userTank.posx;
        let y = userTank.posy;
        let dir = userTank.rotation;

        let newPosX = x;
        let newPosY = y;

        switch (direction) {
            case 'left':
                newPosX -= 1;
                dir = 180;
                break;
            case 'right':
                newPosX += 1;
                dir = 0;
                break;
            case 'up':
                newPosY -= 1;
                dir = -90;
                break;
            case 'down':
                newPosY += 1;
                dir = 90;
                break;
            default:
                console.error('Dirección inválida:', direction);
                //isMoving = false;
                return;
        }

        stompClient.send(`/app/${username}/move`, {}, JSON.stringify({
            posX:x,
            posY:y,
            newPosX:newPosX,
            newPosY:newPosY,
            rotation:dir
        }));
    }

    function rotateTank(tankId, degrees) {
        const tank = document.getElementById(`tank-${tankId}`);
        if (tank) {
            tank.style.transform = `translate(-50%, -50%) rotate(${degrees}deg)`;
        }
    }

    function updateBoard(username) {
        getBoard()
                .then(() => {
                    stompClient.send('/topic/matches/1/movement', {}, JSON.stringify(username));
                })
    }

    function updateTanksBoard() {
        const board = document.getElementById('gameBoard');
        const cells = board.getElementsByClassName('cell');

        // Limpiar el contenido de cada celda antes de redibujar
        for (let cell of cells) {
            cell.innerHTML = '';
        }

        for (let y = 0; y < ROWS; y++) {
            for (let x = 0; x < COLS; x++) {
                const cellIndex = y * COLS + x;
                const cell = cells[cellIndex];

                switch (gameBoard[y][x]) {
                    case '1':  // Muro
                        cell.classList.add('wall');
                        break;

                    case '0':  // Espacio vacío
                        cell.classList.remove('wall');
                        break;

                    default:   // Tanque u otro objeto
                        const tankId = gameBoard[y][x];
                        const tankData = tanks.get(tankId);

                        if (tankData) {
                            const tankElement = document.createElement('div');
                            tankElement.className = 'tank';
                            tankElement.id = `tank-${tankId}`;
                            tankElement.style.backgroundColor = tankData.color;

                            tankElement.style.transform = `translate(-50%, -50%) rotate(${tankData.rotation}deg)`;

                            cell.appendChild(tankElement);
                        }
                        break;
                }
            }
        }
    }

    function updateTankPosition(name, newPosX, newPosY, rotation) {
        const tankElement = document.getElementById(`tank-${name}`);
        if (tankElement) {
            const cells = document.getElementsByClassName('cell');
            const newCellIndex = newPosY * COLS + newPosX;
            cells[newCellIndex].appendChild(tankElement);
            rotateTank(name, rotation);
        }
    }

    const winnerModal = document.getElementById("winnerModal");
    const winnerName = document.getElementById("winnerName");

    // Función para mostrar el modal con el nombre del ganador
    function displayWinner(winner) {
        winnerName.textContent = `¡El ganador es ${winner.name}!`;
        tanks.delete(winner.name);
        initializeBoard();
        winnerModal.style.display = "block"; // Muestra el modal
        resetAfterWin();
    }

    let bullets = new Map();

    function shoot() {
        const bulletId = `bullet-${Date.now()}`;
        console.log(JSON.stringify(bulletId));
        stompClient.send(`/app/${username}/shoot`, {}, bulletId);
        const startX = userTank.posx;
        const startY = userTank.posy;
        const direction = userTank.rotation;
        const bulletData = {
            bulletId: bulletId,
            startX: startX,
            startY: startY,
            direction: direction,
            tankId: username,
            alive: true
        }
        if(username){
            stompClient.send(`/topic/matches/1/bulletAnimation`, {}, JSON.stringify(bulletData));
        }
    }

    function animateBullet(bulletId, startX, startY, direction, tankId) {
        const bullet = document.createElement('div');
        bullet.className = 'bullet';
        bullet.id = `${bulletId}`;
        const cells = document.getElementsByClassName('cell');
        let currentX = startX;
        let currentY = startY;
    
        const initialCellIndex = currentY * COLS + currentX;
        if (initialCellIndex < 0 || initialCellIndex >= cells.length) {
            console.error("Posición inicial fuera de los límites:", initialCellIndex);
            return;
        }
        cells[initialCellIndex].appendChild(bullet);
    
        let dx = 0, dy = 0;
        switch (direction) {
            case 0: dx = 1; break; // Derecha
            case 90: dy = 1; break; // Abajo
            case 180: dx = -1; break; // Izquierda
            case -90: dy = -1; break; // Arriba
        }
        
        function step() {
            if(bullets.get(bulletId).alive){
                bullet.remove(); // Elimina la bala de la celda actual
                currentX += dx;
                currentY += dy;
        
                // Verifica límites del tablero
                if (currentX < 0 || currentX >= COLS || currentY < 0 || currentY >= ROWS) {
                    bullets.delete(bulletId); // Elimina la referencia de la bala
                    return;
                }
        
                const newCellIndex = currentY * COLS + currentX;
                const cellContent = gameBoard[currentY][currentX];
        
                if (cellContent === '0') {
                    cells[newCellIndex].appendChild(bullet);
                    const timeoutId = setTimeout(step, 500);
                    bullets.get(bulletId).timeoutId = timeoutId;
                }
            }
            
        }
        step();
    }
    

    function resetPromise() {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                $.get('/api/tanks/matches/1/reset', function () {
                    resetFront();
                    resolve();
                }).fail(function () {
                    alert("Fallo al reiniciar en el backend");
                    reject(new Error("Failed to reset"));
                });
            }, 10000);
        });
    }

    function resetAfterWin() {
        resetPromise()
                .then(() => {
                    window.location.href = "index.html";
                })
                .catch(error => {
                    console.error("Error al reiniciar:", error);
                });
    }


    function resetFront() {
        tanks = new Map();
        gameBoard = null;
        username = null;
        userTank = null;
        bullets = new Map();

        if (stompClient && stompClient.connected) {
            stompClient.disconnect(() => {
                console.log("Cliente WebSocket desconectado.");
                stompClient = null;
            });
        }
    }

    const styles = document.createElement('style');
    styles.textContent = `
        .bullet {
            width: 10px;
            height: 10px;
            background-color: #ff0000;
            border-radius: 50%;
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            z-index: 100;
        }

        .cell {
            position: relative;
        }
    `;
    document.head.appendChild(styles);
    
    function stopBullet(bulletId) {
        clearTimeout(bullets.get(bulletId).timeoutId); // Cancela el timeout
        //bullets.delete(bulletId); // Elimina la referencia del Map
        console.log(`Animación de la bala ${bulletId} detenida.`);
        
    }

//////////////

    const api = apiClient;
    let username;
    let userTank;
    let gameBoard = [];
    let tanks = new Map();
    let stompClient;

    function placeTanks() {
        tanks.forEach((value, key) => {
            gameBoard[value.posy][value.posx] = value.name;
        });
        const cells = document.getElementsByClassName('cell');
        tanks.forEach((data) => {
            const tankElement = document.createElement('div');
            tankElement.className = 'tank';
            tankElement.id = `tank-${data.name}`;
            tankElement.style.backgroundColor = data.color;
            const cellIndex = data.posy * COLS + data.posx;
            cells[cellIndex].appendChild(tankElement);
        });
    }

    function subscribe(){
        console.info('Connecting to WS...');
        var socket = new SockJS('/stompendpoint');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);

            stompClient.subscribe(`/topic/matches/1/movement`, function (eventbody) {
                const updatedTank = JSON.parse(eventbody.body);
                const name = updatedTank.name;
                if(name === username){
                    userTank = updatedTank;
                }
                const newPosX = updatedTank.posx;
                const newPosY = updatedTank.posy;
                const rotation = updatedTank.rotation;
                updateTankPosition(name, newPosX, newPosY, rotation);
            });

            stompClient.subscribe(`/topic/matches/1/bulletAnimation`, function (eventbody) {
                const bulletData = JSON.parse(eventbody.body);
                const bulletId = bulletData.bulletId;
                const startX = bulletData.startX;
                const startY = bulletData.startY;
                const direction = bulletData.direction;
                const tankId = bulletData.tankId;
                bullets.set(bulletId, bulletData);
                animateBullet(bulletId, startX, startY, direction, tankId);
            });

            stompClient.subscribe('/topic/matches/1/collisionResult', function (eventbody) {
                const data = JSON.parse(eventbody.body);
                const tankDeleted = data.tank;
                tanks.delete(tankDeleted);
                //gameBoard[data.y][data.x] = 0;
                const bulletId = data.bulletId;
                const tankElement = document.getElementById(`tank-${tankDeleted}`);
                $(`#${bulletId}`).remove();
                tankElement.remove();
                bullets.get(bulletId).alive = false;
                stopBullet(bulletId);
                if(tankDeleted === username){
                    username = null;
                }
            });

            stompClient.subscribe('/topic/matches/1/winner', function (message) {
                const winner = JSON.parse(message.body);
                displayWinner(winner);
            });

        });
    }

    function addListeners(){
        document.addEventListener('keydown', (e) => {
            switch (e.key) {
                case 'a':
                    moveTank('left');
                    break;
                case 'd':
                    moveTank('right');
                    break;
                case 'w':
                    moveTank('up');
                    break;
                case 's':
                    moveTank('down');
                    break;
            }
        });

        document.addEventListener('keydown', (e) => {
            if (e.code === 'Space') {
                shoot();
            }
        });
    }

    function initializeBoard() {
        const board = document.getElementById('gameBoard');
        board.innerHTML = '';
        for (let y = 0; y < ROWS; y++) {
            for (let x = 0; x < COLS; x++) {
                const cell = document.createElement('div');
                cell.className = 'cell';
                if (gameBoard[y][x] === '1') {
                    cell.classList.add('wall');
                }
                board.appendChild(cell);
            }
        }
    }

    function init(){
        api.getUsername()
            .then(function(data){
                username = data;
            })

        .then(() => api.getBoard())
            .then(function(board){
                gameBoard = board;
            })
        
        .then(() => initializeBoard())

        .then(() => api.getTank(username))
            .then(function(tank){
                userTank = tank;
            })

        .then(() => api.getTanks(tanks))
            .then(function(data){
                data.forEach(tank => {
                    tanks.set(tank.name, tank);
                });
            })

        .then(() => placeTanks())
        .then(() => subscribe())
        .then(() => addListeners());
    }

    return {
        init: init,
    }
})();