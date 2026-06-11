document.addEventListener("DOMContentLoaded", function () {
  const navbar = document.getElementById("navbar");
  if (navbar) {
    function handleScroll() {
      if (window.scrollY > 50) {
        navbar.classList.add("scrolled");
      } else {
        navbar.classList.remove("scrolled");
      }
    }
    window.addEventListener("scroll", handleScroll);
  }
});
window.addEventListener('pageshow', function (event) {
    if (event.persisted) {
        // Página veio do cache — força reinicialização
        window.location.reload();
    }
});
document.addEventListener("DOMContentLoaded", function () {
  const navbar = document.getElementById("navbar");
  if (navbar) {
    window.addEventListener("scroll", () => {
      if (window.scrollY > 50) navbar.classList.add("scrolled");
      else navbar.classList.remove("scrolled");
    });
  }
});


const relatorioForm = document.getElementById('relatorioForm');
if (relatorioForm) {
    relatorioForm.addEventListener('submit', function(e) {
        const formatoSelecionado = document.querySelector('input[name="formato"]:checked');
        const formato = formatoSelecionado ? formatoSelecionado.value : null;

        if (formato === 'csv') {
            e.preventDefault();
            gerarRelatorioCSV();
        } else if (formato === 'pdf') {
            this.action = '/almoxarifado/relatorio/gerar-pdf';
        }
    });
}

function gerarRelatorioCSV() {
    const tipo = document.getElementById('relatorioTipo').value;
    const dataInicio = document.getElementById('relatorioDataInicio').value;
    const dataFim = document.getElementById('relatorioDataFim').value;

    if (!tipo) {
        alert('Selecione um tipo de relatório');
        return;
    }

    // Busca os dados conforme o tipo selecionado
    fetch(`/almoxarifado/relatorio/dados?tipo=${tipo}&dataInicio=${dataInicio}&dataFim=${dataFim}`)
        .then(response => response.json())
        .then(data => {
            let csvContent = '';

            switch(tipo) {
                case 'estoque':
                    csvContent = gerarCSV_Estoque(data);
                    break;
                case 'solicitacoes':
                    csvContent = gerarCSV_Solicitacoes(data);
                    break;
                case 'movimentacao':
                    csvContent = gerarCSV_Movimentacao(data);
                    break;
            }

            // Cria e faz download do arquivo CSV
            downloadCSV(csvContent, `relatorio_${tipo}_${new Date().toISOString().split('T')[0]}.csv`);
        })
        .catch(error => {
            console.error('Erro ao gerar relatório:', error);
            alert('Erro ao gerar relatório');
        });
}

function gerarCSV_Estoque(data) {
    let csv = 'Nome do Material,Categoria,Quantidade,Validade,Fornecedor\n';

    data.forEach(item => {
        const validade = item.dataValidade ? new Date(item.dataValidade).toLocaleDateString('pt-BR') : 'Sem validade';
        csv += `"${item.nome || ''}","${item.categoria?.nome || 'N/A'}","${item.quantidadeAtual || 0}","${validade}","${item.fornecedor || 'N/A'}"\n`;
    });

    return csv;
}

function gerarCSV_Solicitacoes(data) {
    let csv = 'Material,Quantidade,Solicitante,Data,Tipo de Saída,Status,Justificativa\n';

    data.forEach(item => {
        const dataFormatada = item.dataSolicitacao ? new Date(item.dataSolicitacao).toLocaleString('pt-BR') : '';
        csv += `"${item.material?.nome || 'N/A'}","${item.quantidadeSolicitada || 0}","${item.funcionarioSolicitante?.nomeCompleto || 'N/A'}","${dataFormatada}","${item.tipoSaida || 'N/A'}","${item.status || 'N/A'}","${item.descricao || ''}"\n`;
    });

    return csv;
}

function gerarCSV_Movimentacao(data) {
    let csv = 'Material,Tipo,Quantidade,Data,Responsável,Descrição\n';

    data.forEach(item => {
        const dataFormatada = item.dataMovimentacao ? new Date(item.dataMovimentacao).toLocaleString('pt-BR') : '';
        csv += `"${item.material?.nome || 'N/A'}","${item.tipoMovimentacao || 'N/A'}","${item.quantidade || 0}","${dataFormatada}","${item.responsavel || 'N/A'}","${item.descricao || ''}"\n`;
    });

    return csv;
}

function downloadCSV(csvContent, filename) {
    const blob = new Blob(['\ufeff', csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);

    link.setAttribute('href', url);
    link.setAttribute('download', filename);
    link.style.visibility = 'hidden';

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

const formGerarRelatorio = document.getElementById('formGerarRelatorio');
if (formGerarRelatorio) {
  formGerarRelatorio.addEventListener('submit', function(e) {
    const tipo = this.tipo.value;
    if (!tipo) {
      e.preventDefault();
      alert('Por favor, selecione o tipo de relatório.');
    }
  });
}

document.addEventListener('DOMContentLoaded', function () {
    const inputData = document.getElementById('dataRelatorioDia');
    const inputDataFim = document.getElementById('dataRelatorioDiaFim');

    if (inputData && inputDataFim) {
        inputData.addEventListener('change', function () {
            // quando usuário escolher a data, usamos a MESMA como dataFim
            inputDataFim.value = this.value;
        });
    }
});

    document.addEventListener('DOMContentLoaded', function () {

        // Máscara CPF/CNPJ
        const cpfCnpjInput = document.getElementById('cpfCnpj');
        if (cpfCnpjInput) {
            cpfCnpjInput.addEventListener('input', function (e) {
                let value = e.target.value.replace(/\D/g, '');
                if (value.length <= 11) {
                    value = value.replace(/(\d{3})(\d)/, '$1.$2');
                    value = value.replace(/(\d{3})(\d)/, '$1.$2');
                    value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
                } else {
                    value = value.replace(/(\d{2})(\d)/, '$1.$2');
                    value = value.replace(/(\d{3})(\d)/, '$1.$2');
                    value = value.replace(/(\d{3})(\d)/, '$1/$2');
                    value = value.replace(/(\d{4})(\d{1,2})$/, '$1-$2');
                }
                e.target.value = value;
            });
        }

        // Máscara Telefone
        const telefoneInput = document.getElementById('telefone');
        if (telefoneInput) {
            telefoneInput.addEventListener('input', function (e) {
                let value = e.target.value.replace(/\D/g, '');
                if (value.length > 11) value = value.substring(0, 11);
                if (value.length > 0) value = '(' + value;
                if (value.length > 3) value = value.substring(0, 3) + ') ' + value.substring(3);
                if (value.length > 10) value = value.substring(0, 10) + '-' + value.substring(10);
                e.target.value = value;
            });
        }

    });