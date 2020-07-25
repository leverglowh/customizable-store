import { buildQuote } from './quote.js';

(() => {
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
        fetchUnhandledQuotes();
    };

    const fetchQuotes = () => {
        fetch('GetQuotes').then(res => {
            if (res.status === 401) {
                window.location.href = "index.html";
            }
            res.json().then(data => buildQuoteList(data));
        }).catch(err => {
            alert('Something went wrong : ' + err.message)
            window.location.href = "index.html";
        });
    };

    const buildQuoteList = quotes => {
        const quoteListContainer = document.getElementById('quote-list-container');

        quotes.forEach(q => {
            quoteListContainer.appendChild(buildQuote(q, 'ROLE_EMPLOYEE', false));
        });
    };

    const fetchUnhandledQuotes = () => {
        fetch('GetUnhandledQuotes').then(res => {
            if (res.status === 401) {
                window.location.href = "index.html";
            }
            res.json().then(data => buildUnhandledQuoteList(data));
        }).catch(err => {
            alert('Something went wrong : ' + err)
            window.location.href = "index.html";
        });
    };

    const buildUnhandledQuoteList = quotes => {
        const quoteListContainer = document.getElementById('unhandled-quote-list-container');

        quotes.forEach(q => {
            quoteListContainer.appendChild(buildQuote(q, 'ROLE_EMPLOYEE', true));
            document.getElementById(`price-quote-${q.id}`).addEventListener('click', e => handlePriceQuote(e, q.id) );
        });
    };

    const handlePriceQuote = (e, id) => {
        // disable all other price quotes button
        const buts = document.getElementsByClassName('quote-list-item-price price-quote-button pointer');
        for (let i = 0; i < buts.length; i++) {
            buts[i].disabled = true;
        }
    
        const quoteItem = document.getElementById(`quote-list-item-${id}`);
        const clone = quoteItem.cloneNode(true);
        quoteItem.className += ' smaller-size';
        const infoContainer = quoteItem.getElementsByClassName('quote-list-item-info')[0];
        infoContainer.removeChild(e.target);
    
        const priceForm = document.createElement('form');
        priceForm.id = 'priceQuoteForm';
        const priceInput = document.createElement('input');
        priceInput.className = 'custom-input price-field';
        priceInput.name = 'price';
        priceInput.type = 'text';
        priceInput.placeholder = 'price: 1234.56';
        priceInput.required = true;
        priceInput.id = 'input-price';
    
        const hiddenQuoteId = document.createElement('input');
        hiddenQuoteId.type = 'hidden';
        hiddenQuoteId.name = 'quoteId';
        hiddenQuoteId.value = id;
    
        priceForm.appendChild(priceInput);
        priceForm.appendChild(hiddenQuoteId);
    
        infoContainer.appendChild(priceForm);
    
        // wrap a div
        const parent = quoteItem.parentElement;
        const wrapper = document.createElement('div');
        wrapper.className = 'price-quote-form-container';
        wrapper.id = "price-quote-form-container";
        parent.replaceChild(wrapper, quoteItem);
        wrapper.appendChild(quoteItem);
    
        const buttonsContainer = document.createElement('div');
        buttonsContainer.className = 'price-quote-actions-container';
    
        const cancelButton = document.createElement('button');
        cancelButton.className = 'price-quote-cancel-button pointer';
        cancelButton.type = 'button';
        const cancelIcon = document.createElement('i');
        cancelIcon.className = 'fas fa-times';
        cancelButton.appendChild(cancelIcon);
        
        const submitButton = document.createElement('button');
        submitButton.className = 'price-quote-button pointer';
        submitButton.type = 'button';
        submitButton.innerHTML = 'Price quote';
        buttonsContainer.appendChild(cancelButton);
        buttonsContainer.appendChild(submitButton);
    
        wrapper.appendChild(buttonsContainer);
    
        submitButton.addEventListener('click', () => submitPrice(priceForm));
        cancelButton.addEventListener('click', () => {
            parent.replaceChild(clone, wrapper);
            // restore event listener
            document.getElementById(`price-quote-${id}`).addEventListener('click', e => handlePriceQuote(e, id) );

            // restore buttons
            const buts = document.getElementsByClassName('quote-list-item-price price-quote-button pointer');
            for (let i = 0; i < buts.length; i++) {
                buts[i].disabled = false;
            }
        });
    }
    
    const submitPrice = (form) => {
        const priceValue = Number(document.getElementById('input-price').value);
        if (priceValue && priceValue > 0) {
            fetch('PriceQuote', {
                method: 'POST',
                body: new URLSearchParams(new FormData(form))
            }).then(res => {
                res.json().then(data => {
                    handlePricedQuote(data);
                }).catch(
                    err => alert(err.message)
                );
            }).catch(err => {
                alert('Something went wrong : ' + err);
                form.reset();         
            });
        } else {
            alert('Price not valid');
        }
    }
    
    const handlePricedQuote = quote => {
        const toRemove = document.getElementById('price-quote-form-container');
        const parent = toRemove.parentElement;
        parent.removeChild(toRemove);
    
        const handledQuotesContainer = document.getElementById('quote-list-container');
        handledQuotesContainer.appendChild(buildQuote(quote, 'ROLE_EMPLOYEE', false));

        // restore buttons
        const buts = document.getElementsByClassName('quote-list-item-price price-quote-button pointer');
        for (let i = 0; i < buts.length; i++) {
            buts[i].disabled = false;
        }
    }    

})();