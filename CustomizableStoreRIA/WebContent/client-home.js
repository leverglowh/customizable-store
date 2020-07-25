import { buildQuote } from './quote.js';

(() => {
    let productList;

    window.addEventListener('load', () => {
        let user = sessionStorage.getItem('user');
        if (user === null) {
            window.location.href = 'index.html';
        } else {
            user = JSON.parse(user);
            document.getElementById('welcome-username').textContent = user.username;
            paint();

            document.querySelector("a[href='Logout']").addEventListener('click', () => {
                sessionStorage.clear();
            });
        }
    });

    const paint = () => {
        fetchQuotes();
        fetchProducts();
    };

    const fetchQuotes = () => {
        fetch('GetQuotes').then(res => {
            if (res.status === 401) {
                window.location.href = "index.html";
            }
            res.json().then(data => buildQuoteList(data));
        }).catch(err => {
            alert('Something went wrong : ' + err)
            window.location.href = "index.html";
        });
    };

    const fetchProducts = () => {
        fetch('GetProducts').then(res => {
            if (res.status === 401) {
                window.location.href = "index.html";
            }
            res.json().then(data => buildProductList(data));
        }).catch(err => {
            alert('Something went wrong : ' + err)
            window.location.href = "index.html";
        });
    };

    const buildQuoteList = quotes => {
        const quoteListContainer = document.getElementById('quote-list-container');

        quotes.forEach(q => {
            quoteListContainer.appendChild(buildQuote(q, 'ROLE_CLIENT', false));
        });
    };

    const buildProductList = products => {
        const productListContainer = document.getElementById('product-list-container');

        products.forEach(p => {
            const product = buildProduct(p);
            productListContainer.appendChild(product);
        });

        productList = [...products];
    };

    const buildProduct = p => {
        const product = document.createElement('div');
        product.className = 'product-list-item';
        product.id = `product-${p.id}`;
        const imageContainer = document.createElement('div');
        imageContainer.className = 'product-image-container';
        const image = document.createElement('img');
        image.src = `data:image/jpeg;base64,${p.image}`;
        image.width = 225;
        image.height = 150;
        imageContainer.appendChild(image);
        const infoContainer = document.createElement('div');
        infoContainer.className = 'product-info';
        const prodName = document.createElement('h2');
        prodName.innerText = p.name;
        infoContainer.appendChild(prodName);
        const selectBut = document.createElement('button');
        selectBut.className = 'select-product-button pointer';
        selectBut.id = `select-prod-${p.id}`;
        selectBut.innerText = 'Select';
        product.appendChild(imageContainer);
        product.appendChild(infoContainer);
        product.appendChild(selectBut);

        selectBut.addEventListener('click', onSelectButClick);

        return product;
    }

    const onSelectButClick = e => {
        const prodId = e.target.id.split('-')[2];
        const product = productList.find(p => p.id === Number(prodId));
        const productItem = document.getElementById(`product-${prodId}`);

        // Disable other buttons
        const buttons = document.getElementsByClassName('select-product-button pointer');
        for (let i = 0; i < buttons.length; i++) {
            buttons[i].disabled = true;
        }

        const productListContainer = document.getElementById('product-list-container');
        productListContainer.removeChild(productItem);

        const newProduct = buildExtendedProduct(product);
        productListContainer.insertBefore(newProduct, productListContainer.firstChild);

        const cancelButton = document.getElementById('cancel-button');
        cancelButton.addEventListener('click', () => {
            productListContainer.removeChild(newProduct);
            const buttons = document.getElementsByClassName('select-product-button pointer');
            for (let i = 0; i < buttons.length; i++) {
                buttons[i].disabled = false;
            }
            productListContainer.appendChild(buildProduct(product));
        })

        const submitButton = document.getElementById('submit-quote-request');
        submitButton.addEventListener('click', () => {
            const form = document.getElementsByTagName('form')[0];
            if (form.checkValidity()) {
                fetch('CreateQuote', {
                    method: 'POST',
                    body: new URLSearchParams(new FormData(form))
                }).then(res => {
                    res.json().then(data => {
                        handleNewQuote(data);
                    });
                }).catch(err => {
                    alert('Something went wrong : ' + err);
                    form.reset();
                });
              } else {
                form.reportValidity();
              }
        })
    };

    const buildExtendedProduct = product => {
        const newProduct = document.createElement('div');
        newProduct.className = 'quote-list-item';
        newProduct.id = 'extended-product';
        const imageContainer = document.createElement('div');
        imageContainer.className = 'quote-list-item-image';
        const image = document.createElement('img');
        image.src = `data:image/jpeg;base64,${product.image}`;
        image.width = 225;
        image.height = 150;
        imageContainer.appendChild(image)
        const form = document.createElement('form');
        const prodInfo = document.createElement('div');
        prodInfo.className = 'quote-list-item-info auto-width'
        const cancelButton = document.createElement('i');
        cancelButton.className = 'fas fa-times cancel-button pointer';
        cancelButton.id = 'cancel-button';
        const prodName = document.createElement('h2');
        prodName.innerText = product.name
        const optionListContainer = document.createElement('div');
        optionListContainer.className = 'quote-list-item-option-list';
        const ulEl = document.createElement('ul');
        product.options.forEach(o => {
            const option = document.createElement('li');
            option.className = o.type.localeCompare('ON_SALE') === 0 && 'on-sale';
            
            const label = document.createElement('label');
            label.htmlFor = o.code;
            label.innerText = o.name
            const checkContainer = document.createElement('span');
            const checkInput = document.createElement('input');
            checkInput.id = o.code;
            checkInput.type = 'checkbox';
            checkInput.name = o.id === 1 ? 'baseOptionId' : 'optionIds';
            checkInput.value = o.id;
            checkInput.required = o.id === 1;
            checkContainer.appendChild(checkInput)
            option.appendChild(label);
            option.appendChild(checkContainer)
            ulEl.appendChild(option);
        });
        optionListContainer.appendChild(ulEl)
        const hiddenInput = document.createElement('input');
        hiddenInput.name = 'productId';
        hiddenInput.type = 'hidden';
        hiddenInput.value = product.id;
        const submitButton = document.createElement('button');
        submitButton.className = 'quote-list-item-price pointer';
        submitButton.name = 'create';
        submitButton.innerHTML = 'Create quote'
        submitButton.id = 'submit-quote-request';
        submitButton.type = 'button';
        prodInfo.appendChild(cancelButton);
        prodInfo.appendChild(prodName);
        prodInfo.appendChild(optionListContainer);
        prodInfo.appendChild(hiddenInput);
        prodInfo.appendChild(submitButton)
        form.appendChild(prodInfo)
        newProduct.appendChild(imageContainer)
        newProduct.appendChild(form)
        newProduct.style.flex = '1 0 100%';
        newProduct.style.margin = '20px 0';
        return newProduct;
    }

    const handleNewQuote = q => {
        const quoteContainer = document.getElementById('quote-list-container');
        quoteContainer.appendChild(buildQuote(q, 'ROLE_CLIENT', false));

        const cancelButton = document.getElementById('cancel-button');
        cancelButton.click();
    };
})();